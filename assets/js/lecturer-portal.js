// DIMABIN Management System - Lecturer Portal Logic
// Built strictly to official specifications using modern Firebase Modular SDK.

import { db, auth } from './firebase-init.js';
import { prepareAndLogEmail } from './emailjs-config.js';

// Import Firestore methods dynamically
const {
  collection, doc, getDoc, getDocs, setDoc, updateDoc, query, where, orderBy, limit
} = await import("https://www.gstatic.com/firebasejs/10.8.0/firebase-firestore.js");

// Import Auth methods dynamically
const {
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  updatePassword,
  sendPasswordResetEmail,
  signOut,
  onAuthStateChanged,
  reauthenticateWithCredential,
  EmailAuthProvider
} = await import("https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js");

// Constants and Session Coordinates
const SESSION_KEY = "dimabin_lecturer_session";
const INACTIVITY_LIMIT_MS = 15 * 60 * 1000; // 15 minutes security timeout
let inactivityTimer;
let currentLecturerDoc = null;
let timelineSettings = { session: "2026/2027", semester: "First Semester" };
let officialCoursesList = [];
let allStudentsList = [];
let assignedCoursesData = [];

// Initialize Window Toast if not present
if (!window.showToast) {
  window.showToast = (message, type = "success") => {
    const container = document.getElementById("toastContainer");
    if (!container) {
      alert(`[${type.toUpperCase()}] ${message}`);
      return;
    }
    const toast = document.createElement("div");
    toast.className = `toast-alert ${type}`;
    
    let icon = "fa-circle-check";
    if (type === "error") icon = "fa-triangle-exclamation";
    if (type === "info") icon = "fa-circle-info";
    if (type === "warning") icon = "fa-circle-exclamation";

    toast.innerHTML = `
      <i class="fa-solid ${icon}"></i>
      <span>${message}</span>
    `;
    container.appendChild(toast);
    setTimeout(() => {
      toast.style.animation = "slideOutRight 0.3s ease forwards";
      setTimeout(() => toast.remove(), 300);
    }, 4000);
  };
}

// Inactivity security countdown management
function resetInactivityTimer() {
  clearTimeout(inactivityTimer);
  if (sessionStorage.getItem(SESSION_KEY) || localStorage.getItem(SESSION_KEY)) {
    inactivityTimer = setTimeout(() => {
      handleLogout("Your faculty session expired due to inactivity. Please sign in again.");
    }, INACTIVITY_LIMIT_MS);
  }
}

// Attach inactivity triggers
['click', 'mousemove', 'keypress', 'scroll', 'touchstart'].forEach(evt => {
  document.addEventListener(evt, resetInactivityTimer);
});

// Load App Initial State
document.addEventListener("DOMContentLoaded", async () => {
  try {
    // 1. Fetch Timeline coordinates
    const settingsSnap = await getDoc(doc(db, "settings", "timeline_settings"));
    if (settingsSnap.exists()) {
      const data = settingsSnap.data();
      timelineSettings.session = data.session || timelineSettings.session;
      timelineSettings.semester = data.semester || timelineSettings.semester;
    }
    
    const sessDisp = document.getElementById("currentSessionDisplay");
    const semDisp = document.getElementById("currentSemesterDisplay");
    if (sessDisp) sessDisp.textContent = timelineSettings.session;
    if (semDisp) semDisp.textContent = timelineSettings.semester;

    // 2. Fetch Catalog of all Theology courses
    const coursesSnap = await getDocs(collection(db, "courses"));
    coursesSnap.forEach(cs => {
      officialCoursesList.push({ id: cs.id, ...cs.data() });
    });

    // 3. Setup tabs listeners
    setupTabNavigation();

    // 4. Check for active session
    await checkActiveSession();

  } catch (err) {
    console.error("Initialization failed:", err);
  }
});

// Tab navigation handler
function setupTabNavigation() {
  const tabButtons = document.querySelectorAll(".sidebar-nav-btn[data-tab]");
  tabButtons.forEach(btn => {
    btn.addEventListener("click", () => {
      const targetTab = btn.getAttribute("data-tab");
      switchTab(targetTab);
    });
  });
}

function switchTab(tabId) {
  // Update sidebar active buttons
  const tabButtons = document.querySelectorAll(".sidebar-nav-btn[data-tab]");
  tabButtons.forEach(btn => {
    if (btn.getAttribute("data-tab") === tabId) {
      btn.classList.add("active");
    } else {
      btn.classList.remove("active");
    }
  });

  // Toggle active CSS views
  const contents = document.querySelectorAll(".tab-content");
  contents.forEach(content => {
    if (content.id === `tab-${tabId}`) {
      content.classList.add("active");
      content.style.display = "block";
    } else {
      content.classList.remove("active");
      content.style.display = "none";
    }
  });

  // Re-fetch tab specific data if required
  if (tabId === "courses") renderCoursesTab();
  if (tabId === "students") renderStudentsTab();
  if (tabId === "results") renderResultUploadTab();
  if (tabId === "notifications") renderAnnouncementsTab();
}

// Session Validator
async function checkActiveSession() {
  const sessionDataStr = localStorage.getItem(SESSION_KEY) || sessionStorage.getItem(SESSION_KEY);
  if (sessionDataStr) {
    try {
      const sessionData = JSON.parse(sessionDataStr);
      // Fetch fresh record from Firestore
      const q = query(collection(db, "lecturers"), where("lecturerId", "==", sessionData.lecturerId));
      const snap = await getDocs(q);
      
      if (!snap.empty) {
        currentLecturerDoc = { id: snap.docs[0].id, ...snap.docs[0].data() };
        enterDashboard();
      } else {
        handleLogout("Session invalid. Lecturer record not found.");
      }
    } catch (e) {
      console.error("Session parse failed:", e);
      handleLogout();
    }
  } else {
    // Show clean login panel
    document.getElementById("anonymousView").style.display = "block";
    document.getElementById("authenticatedView").style.display = "none";
    const feedback = document.getElementById("loginFeedback");
    if (feedback) feedback.style.display = "none";
  }
}

// Handle Login Submission
const loginForm = document.getElementById("lecturerLoginForm");
if (loginForm) {
  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const staffIdInput = document.getElementById("staffId").value.trim();
    const passwordInput = document.getElementById("password").value;
    const rememberMe = document.getElementById("rememberMe").checked;

    const feedback = document.getElementById("loginFeedback");
    const feedbackText = document.getElementById("loginFeedbackText");
    const btnSubmit = document.getElementById("btnLoginSubmit");

    if (feedback && feedbackText) {
      feedback.style.display = "flex";
      feedbackText.textContent = "Validating staff profile coordinates...";
    }
    if (btnSubmit) btnSubmit.disabled = true;

    try {
      // 1. Search lecturer record
      const q = query(collection(db, "lecturers"), where("lecturerId", "==", staffIdInput));
      const snap = await getDocs(q);

      if (snap.empty) {
        throw new Error("No facilitator profile discovered with that Staff ID.");
      }

      const lecDoc = { id: snap.docs[0].id, ...snap.docs[0].data() };
      const email = lecDoc.email;

      if (!email || email === "N/A" || email.trim() === "") {
        throw new Error("Facilitator record lacks an institutional email address. Contact Registrars desk.");
      }

      // 2. Authenticate with Firebase Authentication
      let authCredential = null;
      try {
        authCredential = await signInWithEmailAndPassword(auth, email, passwordInput);
      } catch (authErr) {
        // Fallback Auto-Provisioning of the Lecturer Auth user
        if (authErr.code === "auth/user-not-found" || authErr.code === "auth/invalid-credential") {
          const expectedPassword = lecDoc.password || lecDoc.loginCredentials?.password || lecDoc.tempPassword || "dimabin123";
          
          if (expectedPassword === passwordInput) {
            try {
              authCredential = await createUserWithEmailAndPassword(auth, email, passwordInput);
              console.log("🌟 [Auto-Provisioning] Created Firebase Auth account for lecturer:", email);
            } catch (createErr) {
              if (createErr.code === "auth/email-already-in-use") {
                throw new Error("Incorrect login password credential.");
              }
              throw createErr;
            }
          } else {
            throw new Error("Incorrect Staff ID or Password credentials.");
          }
        } else {
          throw authErr;
        }
      }

      // Success - Save Session
      currentLecturerDoc = lecDoc;
      const sessionData = {
        lecturerId: lecDoc.lecturerId,
        fullName: lecDoc.fullName,
        email: lecDoc.email
      };

      if (rememberMe) {
        localStorage.setItem(SESSION_KEY, JSON.stringify(sessionData));
      } else {
        sessionStorage.setItem(SESSION_KEY, JSON.stringify(sessionData));
      }

      // Show welcome toast
      window.showToast("Logged in successfully. Welcome back, Educator!", "success");
      enterDashboard();

    } catch (err) {
      console.error("Login attempt failed:", err);
      if (feedbackText) {
        feedbackText.innerHTML = `<strong>Sign In Error:</strong> ${err.message}`;
      }
      if (feedback) {
        feedback.className = "form-alert error";
        feedback.style.display = "flex";
      }
    } finally {
      if (btnSubmit) btnSubmit.disabled = false;
    }
  });
}

// Reset Password Link Request
const forgotPasswordBtn = document.getElementById("forgotPasswordBtn");
if (forgotPasswordBtn) {
  forgotPasswordBtn.addEventListener("click", async (e) => {
    e.preventDefault();
    const staffIdInput = document.getElementById("staffId").value.trim();
    if (!staffIdInput) {
      window.showToast("Please enter your Staff ID in the login field first.", "warning");
      return;
    }

    try {
      window.showToast("Searching institutional roster...", "info");
      const q = query(collection(db, "lecturers"), where("lecturerId", "==", staffIdInput));
      const snap = await getDocs(q);

      if (snap.empty) {
        throw new Error("Staff ID not registered on this campus.");
      }

      const lecData = snap.docs[0].data();
      const email = lecData.email;

      if (!email || email === "N/A" || email.trim() === "") {
        throw new Error("No active email link mapped on your facilitator profile.");
      }

      await sendPasswordResetEmail(auth, email);
      window.showToast(`A secure cryptographic reset link has been dispatched to ${email}`, "success");

    } catch (err) {
      window.showToast(err.message, "error");
    }
  });
}

// Enter Dashboard Core
async function enterDashboard() {
  document.getElementById("anonymousView").style.display = "none";
  document.getElementById("authenticatedView").style.display = "block";
  resetInactivityTimer();

  // Load name and credentials on sidebar
  const lecNameDisp = document.getElementById("lecturerNameDisplay");
  const lecIdDisp = document.getElementById("lecturerIdDisplay");
  const lecDeptDisp = document.getElementById("lecturerDeptDisplay");
  
  if (lecNameDisp) lecNameDisp.textContent = currentLecturerDoc.fullName;
  if (lecIdDisp) lecIdDisp.textContent = currentLecturerDoc.lecturerId;
  if (lecDeptDisp) lecDeptDisp.textContent = currentLecturerDoc.department || "Theology";

  const welcomeMsg = document.getElementById("welcomeMessage");
  if (welcomeMsg) {
    welcomeMsg.textContent = `Welcome back, ${currentLecturerDoc.fullName}!`;
  }

  // Populate dynamic filters dropdowns
  populateFilterDropdowns();

  // Initialize general data
  await loadCoreDashboardMetrics();
  switchTab("overview");
}

// Load stats badges and metrics
async function loadCoreDashboardMetrics() {
  try {
    const assignedCodes = currentLecturerDoc.coursesAssigned || [];
    
    // 1. Assigned Courses Count
    const assignedCountElement = document.getElementById("cardAssignedCoursesCount");
    if (assignedCountElement) {
      assignedCountElement.textContent = assignedCodes.length;
    }

    // 2. Fetch all registered students for those courses
    const regSnap = await getDocs(collection(db, "registrations"));
    let enrolledCount = 0;
    allStudentsList = []; // Clean roster

    regSnap.forEach(docSnap => {
      const reg = docSnap.data();
      if (reg.academicSession === timelineSettings.session) {
        const studentCourses = reg.registeredCourses || [];
        // Check if there is intersection
        const intersecting = studentCourses.filter(c => assignedCodes.includes(c));
        if (intersecting.length > 0) {
          enrolledCount++;
          intersecting.forEach(code => {
            allStudentsList.push({
              id: reg.studentId,
              fullName: reg.fullName,
              matricNumber: reg.matricNumber,
              courseCode: code,
              programme: reg.programme || "Diploma in Theology",
              academicSession: reg.academicSession
            });
          });
        }
      }
    });

    const registeredCountElement = document.getElementById("cardRegisteredStudentsCount");
    if (registeredCountElement) {
      registeredCountElement.textContent = enrolledCount;
    }

    // 3. Uploaded results count
    const resultsSnap = await getDocs(query(collection(db, "results"), where("academicSession", "==", timelineSettings.session)));
    let uploadedCount = 0;
    resultsSnap.forEach(docSnap => {
      const r = docSnap.data();
      if (assignedCodes.includes(r.courseCode)) {
        uploadedCount++;
      }
    });

    const resultsCountElement = document.getElementById("cardUploadedResultsCount");
    if (resultsCountElement) {
      resultsCountElement.textContent = uploadedCount;
    }

    // 4. Announcements / Notifications count
    const notifSnap = await getDocs(collection(db, "notifications"));
    let announcementsCount = 0;
    notifSnap.forEach(docSnap => {
      const audience = docSnap.data().audience || "all";
      if (audience === "all" || audience === "lecturers") {
        announcementsCount++;
      }
    });

    const notificationsCountElement = document.getElementById("cardNotificationsCount");
    if (notificationsCountElement) {
      notificationsCountElement.textContent = announcementsCount;
    }

  } catch (err) {
    console.error("Failed loading dashboard metrics:", err);
  }
}

// Populate session & courses selection selectors
function populateFilterDropdowns() {
  const sessions = ["2026/2027", "2027/2028", "2025/2026"];
  const assignedCodes = currentLecturerDoc.coursesAssigned || [];

  // Course Selector for students and results tabs
  const studentCourseFilter = document.getElementById("studentsCourseFilter");
  const resultCourseSelector = document.getElementById("resultsCourseSelector");
  const studentEmailSelector = document.getElementById("noticeStudentSelector");

  if (studentCourseFilter) {
    studentCourseFilter.innerHTML = '<option value="all">All Assigned Courses</option>';
    assignedCodes.forEach(code => {
      studentCourseFilter.insertAdjacentHTML("beforeend", `<option value="${code}">${code}</option>`);
    });
  }

  if (resultCourseSelector) {
    resultCourseSelector.innerHTML = '<option value="">-- Choose Assigned Course --</option>';
    assignedCodes.forEach(code => {
      resultCourseSelector.insertAdjacentHTML("beforeend", `<option value="${code}">${code}</option>`);
    });
  }

  // Session Selector for assigned courses & students
  const coursesSessionFilter = document.getElementById("coursesSessionFilter");
  const studentsSessionFilter = document.getElementById("studentsSessionFilter");

  if (coursesSessionFilter) {
    coursesSessionFilter.innerHTML = "";
    sessions.forEach(s => {
      const isSel = s === timelineSettings.session ? "selected" : "";
      coursesSessionFilter.insertAdjacentHTML("beforeend", `<option value="${s}" ${isSel}>${s}</option>`);
    });
    // Add change triggers
    coursesSessionFilter.onchange = () => renderCoursesTab();
  }

  if (studentsSessionFilter) {
    studentsSessionFilter.innerHTML = "";
    sessions.forEach(s => {
      const isSel = s === timelineSettings.session ? "selected" : "";
      studentsSessionFilter.insertAdjacentHTML("beforeend", `<option value="${s}" ${isSel}>${s}</option>`);
    });
    // Add change triggers
    studentsSessionFilter.onchange = () => renderStudentsTab();
  }
}

// Tab 2: Profile Load & Edit
const profileForm = document.getElementById("lecturerProfileForm");
if (profileForm) {
  // Pre-populate read-only details
  switchTab("overview"); // Initialize tabs
  
  // Set values on overview profiles too
  document.getElementById("profileLecturerId").textContent = currentLecturerDoc.lecturerId || "N/A";
  document.getElementById("profileFullName").textContent = currentLecturerDoc.fullName || "N/A";
  document.getElementById("profileDepartment").textContent = currentLecturerDoc.department || "N/A";
  document.getElementById("profileQualification").textContent = currentLecturerDoc.qualification || "N/A";
  
  const assigned = currentLecturerDoc.coursesAssigned || [];
  document.getElementById("profileAssignedCourses").textContent = assigned.join(", ") || "None";

  // Pre-fill fields
  document.getElementById("profilePhone").value = currentLecturerDoc.phone || "";
  document.getElementById("profileEmail").value = currentLecturerDoc.email || "";

  // Edit Submission
  profileForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const phone = document.getElementById("profilePhone").value.trim();
    const email = document.getElementById("profileEmail").value.trim();
    const btnSave = document.getElementById("btnSaveProfile");

    if (btnSave) btnSave.disabled = true;

    try {
      const docRef = doc(db, "lecturers", currentLecturerDoc.id);
      await updateDoc(docRef, { phone, email });

      // Update active session memory
      currentLecturerDoc.phone = phone;
      currentLecturerDoc.email = email;

      window.showToast("Your contact coordinates have been updated successfully.", "success");
      enterDashboard();

    } catch (err) {
      console.error("Profile update failed:", err);
      window.showToast("Profile update failed: " + err.message, "error");
    } finally {
      if (btnSave) btnSave.disabled = false;
    }
  });
}

// Tab 3: Render Assigned Courses Tab
async function renderCoursesTab() {
  const sessionFilterVal = document.getElementById("coursesSessionFilter")?.value || timelineSettings.session;
  const assignedCodes = currentLecturerDoc.coursesAssigned || [];
  
  const tbodyFirst = document.getElementById("firstSemesterCoursesTableBody");
  const tbodySecond = document.getElementById("secondSemesterCoursesTableBody");

  tbodyFirst.innerHTML = `<tr><td colspan="4" style="text-align: center;"><i class="fa-solid fa-spinner fa-spin"></i> Retrieving first semester records...</td></tr>`;
  tbodySecond.innerHTML = `<tr><td colspan="4" style="text-align: center;"><i class="fa-solid fa-spinner fa-spin"></i> Retrieving second semester records...</td></tr>`;

  try {
    // Read registrations for student counting
    const regSnap = await getDocs(collection(db, "registrations"));
    const courseStats = {};
    assignedCodes.forEach(c => { courseStats[c] = 0; });

    regSnap.forEach(docSnap => {
      const reg = docSnap.data();
      if (reg.academicSession === sessionFilterVal) {
        const studentCourses = reg.registeredCourses || [];
        studentCourses.forEach(code => {
          if (assignedCodes.includes(code)) {
            courseStats[code] = (courseStats[code] || 0) + 1;
          }
        });
      }
    });

    // Populate semester tables
    let firstCount = 0;
    let secondCount = 0;
    let htmlFirst = "";
    let htmlSecond = "";

    officialCoursesList.forEach(course => {
      if (assignedCodes.includes(course.courseCode)) {
        const studentCount = courseStats[course.courseCode] || 0;
        const row = `
          <tr>
            <td><strong>${course.courseCode}</strong></td>
            <td>${course.courseTitle}</td>
            <td>${course.creditUnit || course.creditUnits || 3} Units</td>
            <td>
              <span class="status-badge cleared" style="padding: 0.25rem 0.75rem; font-weight: 700;">
                <i class="fa-solid fa-users"></i> ${studentCount} Registered
              </span>
            </td>
          </tr>
        `;

        if (course.semester === "First Semester") {
          htmlFirst += row;
          firstCount++;
        } else {
          htmlSecond += row;
          secondCount++;
        }
      }
    });

    tbodyFirst.innerHTML = firstCount > 0 ? htmlFirst : `<tr><td colspan="4" style="text-align: center; color: var(--text-muted);">No First Semester courses assigned for session ${sessionFilterVal}.</td></tr>`;
    tbodySecond.innerHTML = secondCount > 0 ? htmlSecond : `<tr><td colspan="4" style="text-align: center; color: var(--text-muted);">No Second Semester courses assigned for session ${sessionFilterVal}.</td></tr>`;

  } catch (err) {
    console.error("Error cataloging assigned courses:", err);
    tbodyFirst.innerHTML = `<tr><td colspan="4" style="text-align: center; color: red;">Query error occurred.</td></tr>`;
    tbodySecond.innerHTML = `<tr><td colspan="4" style="text-align: center; color: red;">Query error occurred.</td></tr>`;
  }
}

// Tab 4: Render Registered Students Tab
async function renderStudentsTab() {
  const sessionVal = document.getElementById("studentsSessionFilter")?.value || timelineSettings.session;
  const courseFilterVal = document.getElementById("studentsCourseFilter")?.value || "all";
  const searchVal = document.getElementById("studentsSearchInput")?.value.trim().toLowerCase() || "";

  const tbody = document.getElementById("registeredStudentsTableBody");
  tbody.innerHTML = `<tr><td colspan="5" style="text-align: center;"><i class="fa-solid fa-spinner fa-spin"></i> Filtering enrolled students...</td></tr>`;

  try {
    const assignedCodes = currentLecturerDoc.coursesAssigned || [];
    const regSnap = await getDocs(collection(db, "registrations"));
    
    let rowsHTML = "";
    let filteredCount = 0;
    const recipientDropdown = document.getElementById("noticeStudentSelector");
    const uniqueStudentsForNotice = {};

    regSnap.forEach(docSnap => {
      const reg = docSnap.data();
      if (reg.academicSession === sessionVal) {
        const studentCourses = reg.registeredCourses || [];
        // Intersection of student courses with assigned courses
        let targetCourses = studentCourses.filter(c => assignedCodes.includes(c));
        
        // Apply course filter
        if (courseFilterVal !== "all") {
          targetCourses = targetCourses.filter(c => c === courseFilterVal);
        }

        if (targetCourses.length > 0) {
          // Keep unique student for notices alert selection
          uniqueStudentsForNotice[reg.studentId] = { fullName: reg.fullName, email: reg.email || "N/A" };

          targetCourses.forEach(course => {
            // Apply text searching
            const matchesSearch = 
              reg.fullName.toLowerCase().includes(searchVal) ||
              reg.studentId.toLowerCase().includes(searchVal) ||
              reg.matricNumber.toLowerCase().includes(searchVal);

            if (searchVal === "" || matchesSearch) {
              rowsHTML += `
                <tr>
                  <td><strong>${reg.fullName}</strong></td>
                  <td><code style="background-color: var(--bg-slate); padding: 0.2rem 0.5rem; border-radius: 4px; color: var(--primary); font-family: 'Poppins', sans-serif;">${reg.matricNumber}</code></td>
                  <td><span class="status-badge" style="background-color: var(--bg-slate); color: var(--primary); border: 1px solid var(--border-color); font-weight: 700; padding: 0.25rem 0.75rem;">${course}</span></td>
                  <td>${reg.programme || "Diploma in Theology"}</td>
                  <td>${reg.academicSession}</td>
                </tr>
              `;
              filteredCount++;
            }
          });
        }
      }
    });

    tbody.innerHTML = filteredCount > 0 ? rowsHTML : `<tr><td colspan="5" style="text-align: center; color: var(--text-muted); padding: 2.5rem 0;">No enrolled students match current criteria.</td></tr>`;

    // Populate Recipient selector under EmailJS
    if (recipientDropdown) {
      recipientDropdown.innerHTML = `<option value="">-- Choose Enrolled Student --</option>`;
      Object.keys(uniqueStudentsForNotice).forEach(id => {
        const s = uniqueStudentsForNotice[id];
        recipientDropdown.insertAdjacentHTML("beforeend", `<option value="${id}" data-email="${s.email}" data-name="${s.fullName}">${s.fullName} (${id})</option>`);
      });
    }

  } catch (err) {
    console.error("Error filtering student list:", err);
    tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: red;">Search error occurred.</td></tr>`;
  }
}

// Add real-time trigger for students searching
const searchInput = document.getElementById("studentsSearchInput");
if (searchInput) {
  searchInput.addEventListener("input", () => renderStudentsTab());
}
const courseFilter = document.getElementById("studentsCourseFilter");
if (courseFilter) {
  courseFilter.addEventListener("change", () => renderStudentsTab());
}

// Tab 5: Score Matrix & Result Upload Workspace
let activeCourseGradingList = [];
let activeExistingResultsMap = {};

async function renderResultUploadTab() {
  const selector = document.getElementById("resultsCourseSelector");
  if (!selector) return;

  // Clear sheet workspace on load
  document.getElementById("gradeSheetWorkspace").style.display = "none";
  document.getElementById("noCourseSelectedAlert").style.display = "block";
  document.getElementById("resultsStatusIndicator").style.display = "none";

  selector.onchange = async () => {
    const code = selector.value;
    if (!code) {
      document.getElementById("gradeSheetWorkspace").style.display = "none";
      document.getElementById("noCourseSelectedAlert").style.display = "block";
      document.getElementById("resultsStatusIndicator").style.display = "none";
      return;
    }

    document.getElementById("noCourseSelectedAlert").style.display = "none";
    const workspace = document.getElementById("gradeSheetWorkspace");
    const tbody = document.getElementById("gradingWorkspaceTableBody");

    tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; padding: 3rem 0;"><i class="fa-solid fa-spinner fa-spin"></i> Initializing Grading Sheet Workspace...</td></tr>`;
    workspace.style.display = "block";

    try {
      // 1. Fetch existing results
      activeExistingResultsMap = {};
      const resultsQuery = query(
        collection(db, "results"),
        where("courseCode", "==", code),
        where("academicSession", "==", timelineSettings.session)
      );
      const resSnap = await getDocs(resultsQuery);
      let publishedCount = 0;
      let draftCount = 0;

      resSnap.forEach(docSnap => {
        const data = docSnap.data();
        activeExistingResultsMap[data.studentId] = { id: docSnap.id, ...data };
        if (data.status === "Published") publishedCount++;
        else draftCount++;
      });

      // Update status banner indicator
      const statusInd = document.getElementById("resultsStatusIndicator");
      if (statusInd) {
        statusInd.style.display = "inline-block";
        if (publishedCount > 0 && draftCount === 0) {
          statusInd.className = "status-badge cleared";
          statusInd.innerHTML = `<i class="fa-solid fa-circle-check"></i> Published`;
        } else if (draftCount > 0) {
          statusInd.className = "status-badge pending";
          statusInd.innerHTML = `<i class="fa-solid fa-file-pen"></i> Draft Version Active`;
        } else {
          statusInd.className = "status-badge info";
          statusInd.innerHTML = `<i class="fa-solid fa-circle-info"></i> Pending Evaluation`;
        }
      }

      // 2. Fetch enrolled students
      const regSnap = await getDocs(collection(db, "registrations"));
      activeCourseGradingList = [];
      
      regSnap.forEach(docSnap => {
        const reg = docSnap.data();
        if (reg.academicSession === timelineSettings.session) {
          const registered = reg.registeredCourses || [];
          if (registered.includes(code)) {
            activeCourseGradingList.push({
              studentId: reg.studentId,
              fullName: reg.fullName,
              matricNumber: reg.matricNumber,
              courseCode: code,
              semester: timelineSettings.semester,
              academicSession: timelineSettings.session
            });
          }
        }
      });

      if (activeCourseGradingList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--text-muted); padding: 3rem 0;">No enrolled student found registered in ${code} for this session.</td></tr>`;
        return;
      }

      // Render workspace rows
      tbody.innerHTML = "";
      activeCourseGradingList.forEach(student => {
        const exist = activeExistingResultsMap[student.studentId] || {};
        const caVal = exist.caScore !== undefined ? exist.caScore : "";
        const examVal = exist.examScore !== undefined ? exist.examScore : "";
        const totalVal = exist.score !== undefined ? exist.score : "-";
        const gradeVal = exist.grade !== undefined ? exist.grade : "-";
        const statusText = exist.status || "Draft";
        const isPub = statusText === "Published";

        const row = `
          <tr data-student-id="${student.studentId}">
            <td><strong>${student.fullName}</strong></td>
            <td><code>${student.matricNumber}</code></td>
            <td>
              <input type="number" class="form-control ca-input" min="0" max="30" placeholder="CA (0-30)" value="${caVal}" style="width: 100%; text-align: center; padding: 0.4rem; background-color: var(--bg-slate); border: 1px solid var(--border-color); border-radius: 4px;" ${isPub ? 'disabled' : ''}>
            </td>
            <td>
              <input type="number" class="form-control exam-input" min="0" max="70" placeholder="Exam (0-70)" value="${examVal}" style="width: 100%; text-align: center; padding: 0.4rem; background-color: var(--bg-slate); border: 1px solid var(--border-color); border-radius: 4px;" ${isPub ? 'disabled' : ''}>
            </td>
            <td style="text-align: center; font-weight: 700; color: var(--primary);" class="row-total-score">${totalVal}</td>
            <td style="text-align: center;" class="row-grade">
              <span class="status-badge ${gradeVal === 'F' ? '' : 'cleared'}" style="padding: 0.2rem 0.6rem; font-size: 0.85rem; font-weight: 800;">${gradeVal}</span>
            </td>
            <td style="text-align: center;" class="row-result-pass-fail">-</td>
            <td style="text-align: center;">
              <span class="status-badge ${isPub ? 'cleared' : 'pending'}" style="padding: 0.2rem 0.6rem; font-size: 0.85rem; font-weight: 700;" class="row-workflow-status">
                ${statusText}
              </span>
            </td>
          </tr>
        `;
        tbody.insertAdjacentHTML("beforeend", row);
      });

      // Bind dynamic auto-calculation listeners to the input fields
      setupGradingCalculationTriggers();

    } catch (err) {
      console.error("Grading load failed:", err);
      tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: red;">Workspace compilation error occurred.</td></tr>`;
    }
  };
}

// Interactive Matrix calculation logic (CA + Exam)
function setupGradingCalculationTriggers() {
  const tbody = document.getElementById("gradingWorkspaceTableBody");
  const rows = tbody.querySelectorAll("tr[data-student-id]");

  rows.forEach(row => {
    const caInput = row.querySelector(".ca-input");
    const examInput = row.querySelector(".exam-input");
    const totalScoreCell = row.querySelector(".row-total-score");
    const gradeCell = row.querySelector(".row-grade");
    const passFailCell = row.querySelector(".row-result-pass-fail");

    function calculate() {
      const ca = parseFloat(caInput.value) || 0;
      const exam = parseFloat(examInput.value) || 0;

      // Ensure valid boundaries
      if (caInput.value !== "" && (ca < 0 || ca > 30)) {
        window.showToast("Continuous Assessment scores must range between 0 and 30.", "warning");
        caInput.value = Math.max(0, Math.min(30, ca));
        return;
      }
      if (examInput.value !== "" && (exam < 0 || exam > 70)) {
        window.showToast("Semester Examination scores must range between 0 and 70.", "warning");
        examInput.value = Math.max(0, Math.min(70, exam));
        return;
      }

      if (caInput.value === "" && examInput.value === "") {
        totalScoreCell.textContent = "-";
        gradeCell.innerHTML = `<span class="status-badge" style="padding: 0.2rem 0.6rem; font-size: 0.85rem; font-weight: 800;">-</span>`;
        passFailCell.innerHTML = `-`;
        return;
      }

      const total = Math.min(100, Math.round(ca + exam));
      totalScoreCell.textContent = total;

      // Grade Determination (A-F Matrix)
      let grade = "F";
      let isPass = false;
      if (total >= 70) { grade = "A"; isPass = true; }
      else if (total >= 60) { grade = "B"; isPass = true; }
      else if (total >= 50) { grade = "C"; isPass = true; }
      else if (total >= 40) { grade = "D"; isPass = true; }

      // Update cells
      gradeCell.innerHTML = `
        <span class="status-badge ${grade === 'F' ? '' : 'cleared'}" style="padding: 0.2rem 0.6rem; font-size: 0.85rem; font-weight: 800;">
          ${grade}
        </span>
      `;

      passFailCell.innerHTML = `
        <span class="status-badge ${isPass ? 'cleared' : ''}" style="padding: 0.2rem 0.5rem; font-size: 0.8rem; font-weight: 700;">
          ${isPass ? 'PASS' : 'FAIL'}
        </span>
      `;
    }

    caInput.addEventListener("input", calculate);
    examInput.addEventListener("input", calculate);
    
    // Initial compute on load
    calculate();
  });
}

// Bulk Actions: Save Draft and Publish
const btnSaveDraft = document.getElementById("btnSaveDraftResults");
const btnPublish = document.getElementById("btnPublishResults");

if (btnSaveDraft) {
  btnSaveDraft.addEventListener("click", () => handleResultSubmissionFlow("Draft"));
}
if (btnPublish) {
  btnPublish.addEventListener("click", () => handleResultSubmissionFlow("Published"));
}

async function handleResultSubmissionFlow(targetStatus) {
  const courseCode = document.getElementById("resultsCourseSelector").value;
  if (!courseCode) {
    window.showToast("Select a course to submit results.", "warning");
    return;
  }

  if (targetStatus === "Published") {
    const confirmPub = confirm("Are you sure you want to PUBLISH these results?\nOnce published, they are permanently locked and made visible to all student portals instantly.");
    if (!confirmPub) return;
  }

  const tbody = document.getElementById("gradingWorkspaceTableBody");
  const rows = tbody.querySelectorAll("tr[data-student-id]");
  let successfulSaves = 0;

  if (btnSaveDraft) btnSaveDraft.disabled = true;
  if (btnPublish) btnPublish.disabled = true;

  window.showToast(`Processing academic grades matrix (${targetStatus})...`, "info");

  try {
    for (let row of rows) {
      const studentId = row.getAttribute("data-student-id");
      const caInput = row.querySelector(".ca-input");
      const examInput = row.querySelector(".exam-input");

      // Skip empty inputs
      if (caInput.value === "" && examInput.value === "") continue;

      // Skip already published records to prevent overwrites
      const existing = activeExistingResultsMap[studentId] || {};
      if (existing.status === "Published") continue;

      const ca = parseFloat(caInput.value) || 0;
      const exam = parseFloat(examInput.value) || 0;
      const total = Math.min(100, Math.round(ca + exam));

      let grade = "F";
      if (total >= 70) grade = "A";
      else if (total >= 60) grade = "B";
      else if (total >= 50) grade = "C";
      else if (total >= 40) grade = "D";

      // Result document schema
      const docId = `${studentId.replace(/\//g, "-")}_${courseCode}_${timelineSettings.session.replace(/\//g, "-")}`;
      const payload = {
        studentId: studentId,
        courseCode: courseCode,
        caScore: ca,
        examScore: exam,
        score: total,
        grade: grade,
        semester: timelineSettings.semester,
        academicSession: timelineSettings.session,
        status: targetStatus,
        uploadedBy: currentLecturerDoc.lecturerId,
        uploadedAt: new Date().toISOString()
      };

      await setDoc(doc(db, "results", docId), payload);
      successfulSaves++;
    }

    window.showToast(`Successfully registered ${successfulSaves} student grades.`, "success");
    // Reload active selector workspace
    document.getElementById("resultsCourseSelector").dispatchEvent(new Event("change"));

  } catch (err) {
    console.error("Grades matrix dispatch failed:", err);
    window.showToast("Matrix storage error: " + err.message, "error");
  } finally {
    if (btnSaveDraft) btnSaveDraft.disabled = false;
    if (btnPublish) btnPublish.disabled = false;
  }
}

// Tab 6: Announcements List and EmailJS student dispatcher
async function renderAnnouncementsTab() {
  const container = document.getElementById("lecturerAnnouncementsContainer");
  if (!container) return;

  container.innerHTML = `<p style="text-align: center; color: var(--text-muted); padding: 2rem 0;"><i class="fa-solid fa-spinner fa-spin"></i> Retrieving institutional notice logs...</p>`;

  try {
    const q = query(collection(db, "notifications"), orderBy("createdAt", "desc"));
    const snap = await getDocs(q);

    let html = "";
    let count = 0;

    snap.forEach(docSnap => {
      const n = docSnap.data();
      const aud = n.audience || "all";

      if (aud === "all" || aud === "lecturers") {
        const dateStr = n.createdAt ? new Date(n.createdAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' }) : "Active";
        html += `
          <div style="border-left: 4px solid var(--accent); padding: 1.25rem; background-color: var(--bg-slate); border-radius: 4px; border: 1px solid var(--border-color); border-left-width: 4px;">
            <div style="display: flex; justify-content: space-between; margin-bottom: 0.5rem; font-size: 0.85rem; color: var(--text-muted);">
              <strong><i class="fa-solid fa-calendar-day"></i> Published: ${dateStr}</strong>
              <span class="status-badge cleared" style="font-size: 0.75rem; padding: 0.1rem 0.5rem;">${aud.toUpperCase()}</span>
            </div>
            <h4 class="font-display" style="color: var(--primary); font-size: 1.05rem; margin-top: 0; margin-bottom: 0.5rem;">${n.title || "Announcement Notice"}</h4>
            <p style="margin: 0; font-size: 0.9rem; line-height: 1.5; color: var(--text-dark);">${n.message}</p>
          </div>
        `;
        count++;
      }
    });

    container.innerHTML = count > 0 ? html : `<p style="text-align: center; color: var(--text-muted); padding: 2rem 0;">No active campus notices available today.</p>`;

  } catch (err) {
    console.error("Announcement logs fetch failure:", err);
    container.innerHTML = `<p style="text-align: center; color: red; padding: 2rem 0;"><i class="fa-solid fa-triangle-exclamation"></i> Notices lookup failed.</p>`;
  }
}

// EmailJS Form alert dispatcher
const emailAlertForm = document.getElementById("studentEmailAlertForm");
if (emailAlertForm) {
  emailAlertForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const selector = document.getElementById("noticeStudentSelector");
    const option = selector.options[selector.selectedIndex];

    if (!selector.value) {
      window.showToast("Please choose an enrolled student as recipient.", "warning");
      return;
    }

    const recipientName = option.getAttribute("data-name");
    const recipientEmail = option.getAttribute("data-email");
    const subject = document.getElementById("noticeSubject").value.trim();
    const message = document.getElementById("noticeMessage").value.trim();
    const btnSend = document.getElementById("btnSendStudentEmail");

    if (btnSend) btnSend.disabled = true;
    window.showToast("Compiling EmailJS transaction payload...", "info");

    try {
      if (!recipientEmail || recipientEmail === "N/A" || recipientEmail.trim() === "") {
        throw new Error("Target student has no registered email coordinates.");
      }

      // Payload parameters mapped to EmailJS student notice
      const templateParams = {
        subject: subject,
        message: message,
        lecturer_name: currentLecturerDoc.fullName,
        lecturer_id: currentLecturerDoc.lecturerId,
        lecturer_department: currentLecturerDoc.department || "Theology Dept"
      };

      const result = await prepareAndLogEmail("student", recipientName, recipientEmail, templateParams);
      
      if (result.success) {
        if (result.mode === "simulation") {
          window.showToast("Simulation Active: Notice logged to system console successfully.", "info");
        } else {
          window.showToast("Institutional notice successfully dispatched through EmailJS APIs.", "success");
        }
        // Clear inputs
        document.getElementById("noticeSubject").value = "";
        document.getElementById("noticeMessage").value = "";
        selector.value = "";
      } else {
        throw new Error(result.error);
      }

    } catch (err) {
      console.error("EmailJS submission failure:", err);
      window.showToast(err.message, "error");
    } finally {
      if (btnSend) btnSend.disabled = false;
    }
  });
}

// Tab 7: Change Password
const changePasswordForm = document.getElementById("changeLecturerPasswordForm");
if (changePasswordForm) {
  changePasswordForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const currentPass = document.getElementById("securityCurrentPassword").value;
    const newPass = document.getElementById("securityNewPassword").value;
    const confirmPass = document.getElementById("securityConfirmPassword").value;
    const btnUpdate = document.getElementById("btnUpdatePassword");

    if (newPass !== confirmPass) {
      window.showToast("New passwords do not match.", "warning");
      return;
    }

    if (newPass.length < 6) {
      window.showToast("Passwords must contain at least 6 characters.", "warning");
      return;
    }

    if (btnUpdate) btnUpdate.disabled = true;
    window.showToast("Authenticating current cryptograph credentials...", "info");

    try {
      const user = auth.currentUser;
      if (!user) {
        throw new Error("No active Firebase Authentication context discovered. Re-login.");
      }

      // Re-authenticate user context safely
      const credential = EmailAuthProvider.credential(user.email, currentPass);
      await reauthenticateWithCredential(user, credential);

      // Commit update in Auth
      await updatePassword(user, newPass);

      // Commit update in Firestore to keep loginCredentials/tempPassword synced for future fallback logins
      const docRef = doc(db, "lecturers", currentLecturerDoc.id);
      await updateDoc(docRef, {
        password: newPass,
        "loginCredentials.password": newPass,
        tempPassword: newPass
      });

      // Clear fields
      document.getElementById("securityCurrentPassword").value = "";
      document.getElementById("securityNewPassword").value = "";
      document.getElementById("securityConfirmPassword").value = "";

      window.showToast("Your cryptographic password credentials have been updated successfully.", "success");

    } catch (err) {
      console.error("Credentials update failure:", err);
      window.showToast("Credential updating error: " + err.message, "error");
    } finally {
      if (btnUpdate) btnUpdate.disabled = false;
    }
  });
}

// Sign Out Procedure
window.handleLogout = async (customMessage = null) => {
  try {
    await signOut(auth);
  } catch (err) {
    console.error("Auth signOut failed:", err);
  }

  // Wipe storage coordinates
  localStorage.removeItem(SESSION_KEY);
  sessionStorage.removeItem(SESSION_KEY);
  currentLecturerDoc = null;

  // Clear timer
  clearTimeout(inactivityTimer);

  // Restore login views
  document.getElementById("anonymousView").style.display = "block";
  document.getElementById("authenticatedView").style.display = "none";

  // Hide indicator on login
  const feedback = document.getElementById("loginFeedback");
  if (feedback) feedback.style.display = "none";

  // Clear inputs
  if (loginForm) loginForm.reset();

  if (customMessage) {
    alert(customMessage);
  } else {
    window.showToast("Signed out successfully.", "success");
  }
};

// Bind Signout UI buttons
const btnLogout = document.getElementById("btnLogout");
if (btnLogout) {
  btnLogout.addEventListener("click", () => window.handleLogout());
}
