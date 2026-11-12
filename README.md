# Keep Focus (Free)

A local-first Android app project. Extract this ZIP, create a GitHub repository, push the project root, and GitHub Actions will build a debug APK. With signing secrets configured, it also builds a signed release APK and Play-ready AAB.

## Important package ID

The project currently uses `com.keepfocus.app` in `app/build.gradle.kts`, the manifest service declarations and Kotlin package declarations. For an existing Play Store app update, this MUST exactly match the published app's application ID. Change all occurrences consistently before building if your published package differs. The original uploaded KeepFocus ZIP contained only Gradle setup files and no application source or package metadata, so its published ID could not be recovered.

## Build through GitHub

1. Create a new GitHub repository.
2. Extract `KeepFocus-Free.zip` and upload the contents of the `KeepFocus` folder to the repository root (the `.github` folder must be included).
3. Commit to the `main` branch. Open **Actions** and download `KeepFocus-APK` after the workflow completes.
4. The debug APK is for testing only. Do not upload it to Play Console as a production update.

## Create a release keystore

Run locally (keep this file private and make a secure backup):

```bash
keytool -genkeypair -v -keystore keepfocus-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias keepfocus
base64 -i keepfocus-release.jks | pbcopy
# Linux: base64 -w 0 keepfocus-release.jks
```

Add these repository secrets in **Settings → Secrets and variables → Actions**:

- `KEYSTORE_BASE64`: one-line base64 output of the JKS file
- `KEYSTORE_PASSWORD`: keystore password
- `KEY_ALIAS`: alias (for example `keepfocus`)
- `KEY_PASSWORD`: key password

When all four secrets exist, the workflow creates a signed release APK and AAB. Without them, the debug APK still builds. Never commit the keystore or passwords.

## Play Store version updates

At the top of `app/build.gradle.kts`, increment `appVersionCode` for every upload (for example 1 → 2) and update `appVersionName` (for example `1.0.0` → `1.0.1`). Keep the signing key and package ID unchanged for updates.

## Local build

Requires JDK 17 and Android SDK Platform 36 / Build Tools installed. Run `./gradlew :app:assembleDebug` (macOS/Linux) or `gradlew.bat :app:assembleDebug` (Windows).

## Accessibility behavior and limitations

The service subscribes only to Instagram and YouTube scroll/window events. There are no stable public Reels/Shorts view IDs: package + scroll events are a best-effort heuristic and can also represent scrolling outside the short-video feed. App UI changes can affect detection. The service deliberately does not inspect text or content descriptions. Validate on real devices and review Google Play's Accessibility API declaration before release. Keep Focus is a focus utility, not an accessibility tool; it does not set `isAccessibilityTool=true`.

The reminder uses a transparent full-screen Activity because Android may restrict background activity launches and SYSTEM_ALERT_WINDOW does not guarantee unrestricted launches. If Android blocks the activity, a notification/foreground UX would be the compliant fallback; this build does not claim guaranteed interruption on every OEM/device. It requests no internet permission and sends no data.

## Manual real-phone checklist

- Install debug APK on Android 8.0+ and complete onboarding.
- Confirm profile validation (required name/dream/age, age 10–100).
- Pick a photo, restart the app, and verify it remains available (private copy).
- Enable Keep Focus in Android Accessibility settings and explicitly grant overlay access if desired.
- Test Instagram Reels and YouTube Shorts separately; verify counter debounce, threshold, app-switch reset and screen-off behavior.
- Verify reminder visibility, 5-second button lockout, close-app action and repeated cycles.
- Test with screen locked, battery saver, OEM background restrictions, dark mode, and Android 14/15/16 devices.
- Inspect Play Console Accessibility declaration, Data Safety answers and privacy policy. Obtain policy approval before production rollout.

## Known scope

This is a complete starter project with onboarding, local preferences, Room daily-stat storage, a basic home/settings UI, counter tests, accessibility event monitoring, and release workflow. Detection of a specific Reels/Shorts feed cannot be guaranteed by Android APIs because target apps expose no stable public feed identifier. Stats for raw reel swipes are not yet aggregated to Room in this initial release; the reminder count is persisted. Treat this as a test build until the manual QA checklist and Play review are completed.
