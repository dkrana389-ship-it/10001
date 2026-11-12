# Keep Focus - SDK 36 compatibility fix

## Root cause from the GitHub Actions log

The failing task was `:app:checkDebugAarMetadata`.

The log reported that Coil 3.6.3 Android artifacts require compileSdk 37, while this project compiles against Android API 36. The project also used Compose BOM 2026.09.00, which manages Compose 1.12.x; Compose 1.12 requires compileSdk 37 and AGP 9 or newer.

## Changes in this archive

1. Removed Coil 3 from the version catalog and app dependencies. No application source file imports Coil or calls `AsyncImage`, so it was unused.
2. Changed Compose BOM from `2026.09.00` to `2026.06.00`, a published stable BOM from the Compose 1.11 release line. Compose 1.12 / API 37 requirements are therefore not pulled in by the BOM.
3. Kept `compileSdk = 36`, `targetSdk = 36`, AGP 8.13.2, and Gradle 8.13. AGP 8.13 supports Gradle 8.13 and API 36.
4. Kept the GitHub Actions workflow: it builds and uploads the debug APK first; signed release APK/AAB tasks run only when all signing secrets exist.

## Build verification status

The source ZIP was structurally checked and the dependency references were cross-checked against the reported AAR metadata failure. A real Gradle compile was not run in this environment because it cannot reliably access the Gradle distribution/dependency repositories. GitHub Actions must confirm compilation.

The application ID remains `com.keepfocus.app`. It must exactly match the existing Play Store app's package ID before an update can be published.
