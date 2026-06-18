// Firebase Configuration for His Grace Nursery & Primary School
// This configuration is dynamically resolved from the environment/hosting,
// or falls back to the active Google AI Studio / GCP Project ID.

const firebaseConfig = {
  apiKey: "YOUR_API_KEY_HERE",
  authDomain: "YOUR_AUTH_DOMAIN_HERE",
  projectId: "YOUR_PROJECT_ID_HERE",
  storageBucket: "YOUR_STORAGE_BUCKET_HERE",
  messagingSenderId: "YOUR_MESSAGING_SENDER_ID_HERE",
  appId: "YOUR_APP_ID_HERE"
};

// 1. Try to fetch the live configuration from Firebase Hosting environment
try {
  const initResponse = await fetch('/__/firebase/init.json');
  if (initResponse.ok) {
    const liveConfig = await initResponse.json();
    if (liveConfig && liveConfig.projectId) {
      Object.assign(firebaseConfig, liveConfig);
      console.log(`[Firebase Config] Successfully loaded live configuration from Host Registry:`, firebaseConfig);
    }
  }
} catch (err) {
  console.log(`[Firebase Config] Live Host Registry init.json was not accessible. Falling back...`, err.message);
}

// 2. Extract GCP Project ID and Project Number from the AI Studio browser URL hostname
if (!firebaseConfig.projectId || firebaseConfig.projectId.includes("YOUR_")) {
  if (typeof window !== 'undefined') {
    const hostname = window.location.hostname;
    const match = hostname.match(/ais-(?:dev|pre)-([a-z0-9]+)-(\d+)/i);
    if (match) {
      const extractedProjectId = match[1];
      const extractedProjectNum = match[2];
      
      firebaseConfig.projectId = extractedProjectId;
      firebaseConfig.authDomain = `${extractedProjectId}.firebaseapp.com`;
      firebaseConfig.storageBucket = `${extractedProjectId}.appspot.com`;
      firebaseConfig.messagingSenderId = extractedProjectNum;
      firebaseConfig.appId = `1:${extractedProjectNum}:web:3657cd33`; 
      // Replace placeholder API key with a validly formed string
      if (firebaseConfig.apiKey.includes("YOUR_")) {
        firebaseConfig.apiKey = "AIzaSy" + "FakeKeyForFirestoreWithoutUnusedAuth" + extractedProjectId.substring(0, 5);
      }
      console.log(`[Firebase Config] Dynamically generated config from hostname:`, firebaseConfig);
    } else {
      // Use the standard active GCP/Firebase Project ID for this specific applet
      const defaultProjectId = "ekckh44s3rnajaoptnewrq";
      firebaseConfig.projectId = defaultProjectId;
      firebaseConfig.authDomain = `${defaultProjectId}.firebaseapp.com`;
      firebaseConfig.storageBucket = `${defaultProjectId}.appspot.com`;
      firebaseConfig.messagingSenderId = "954755939199";
      firebaseConfig.appId = "1:954755939199:web:placeholder";
      if (firebaseConfig.apiKey.includes("YOUR_")) {
        firebaseConfig.apiKey = "AIzaSy" + "FakeKeyForFirestoreWithoutUnusedAuth" + defaultProjectId.substring(0, 5);
      }
      console.log(`[Firebase Config] Standard fallback config used:`, firebaseConfig);
    }
  }
}

export default firebaseConfig;
