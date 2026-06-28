// Dedicated Firebase Initialization File for Divine Mandate Bible Institute (DIMABIN)
import firebaseConfig from './firebase-config-env.js';

let app = null;
let auth = null;
let db = null;
let storage = null;

try {
  // Always import Firebase SDK from secure CDN for web environments
  const { initializeApp } = await import("https://www.gstatic.com/firebasejs/10.8.0/firebase-app.js");
  const { getAuth } = await import("https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js");
  const { getFirestore } = await import("https://www.gstatic.com/firebasejs/10.8.0/firebase-firestore.js");
  const { getStorage } = await import("https://www.gstatic.com/firebasejs/10.8.0/firebase-storage.js");

  app = initializeApp(firebaseConfig);
  auth = getAuth(app);
  db = getFirestore(app);
  storage = getStorage(app);
  
  console.log(`🌟 [Firebase Init] Firebase successfully connected and initialized! Project: "${firebaseConfig.projectId}"`);
} catch (error) {
  console.error("❌ [Firebase Init] Failed to initialize Firebase:", error);
}

export { app, auth, db, storage };
