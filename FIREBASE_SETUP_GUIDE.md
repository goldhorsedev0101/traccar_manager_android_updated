# Firebase Configuration Setup Guide

## Step 1: Download google-services.json

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Sign in with:
   - Email: gpslink.usa@gmail.com
   - Password: Pedro5502*
3. Select your project (or create a new one if needed)
4. Click the **gear icon** (⚙️) next to "Project Overview"
5. Select **Project Settings**
6. Scroll down to **Your apps** section
7. If you don't have an Android app registered:
   - Click **Add app** → Select **Android** icon
   - Enter package name: `gpslink.traccar.manager`
   - Enter app nickname (optional): "Traccar Manager"
   - Click **Register app**
8. Download the `google-services.json` file
9. Place it in: `app/google-services.json` (replace the existing placeholder)

## Step 2: Verify Firebase Services

Make sure these services are enabled in Firebase Console:
- ✅ Cloud Messaging (FCM) - for push notifications
- ✅ Analytics (optional but recommended)
- ✅ Crashlytics (optional but recommended)

## Step 3: Verify the Configuration

After placing the file, the build will automatically use it. The file should contain:
- Your actual project number
- Your actual Firebase API keys
- Package name: `gpslink.traccar.manager`

## Step 4: Rebuild (Optional)

After updating google-services.json, you can rebuild:
```bash
./gradlew clean bundleGoogleRelease
```

## Important Security Notes

⚠️ **Never commit google-services.json to public repositories**
- The file contains API keys (though they're safe to include in the app)
- Add to `.gitignore` if not already there
   