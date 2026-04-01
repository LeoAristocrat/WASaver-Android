# WASaver

WASaver is an Android app for saving WhatsApp statuses, browsing private media, recovering deleted messages from notifications, and opening direct chats without saving contacts.

[Download the latest release](https://github.com/LeoAristocrat/WASaver-Android/releases)

## Highlights

- Save photo and video statuses from both WhatsApp and WhatsApp Business
- Browse private image and video folders before media disappears
- Manage saved media with search, sorting, and bulk actions
- Recover deleted messages locally through notification capture
- Open direct chats without adding contacts first
- Check GitHub releases from inside the app
- Light and dark themes with a softer pastel UI refresh

## Main Features

### Status Viewer

- Switch between WhatsApp and WA Business
- Filter by all, photos, or videos
- Open statuses in a full-screen viewer
- Save, repost, or share media directly
- Multi-select for bulk save and share

### Media Browser

- Browse WhatsApp private image and video folders
- Save media before it disappears
- Separate access flow for image and video folders
- Search, sort, and bulk actions

### Saved Statuses

- Browse everything you have saved in one place
- Search and sort saved items
- Multi-select and bulk delete

### Deleted Messages

- Capture WhatsApp notifications locally on your device
- Group messages by sender
- Search chats and messages
- Clear individual chats or wipe everything
- Works only after you explicitly enable it

### Direct Message

- Open a chat with any number without saving it
- Supports WhatsApp and WA Business
- Optional pre-filled message

### Updates

- Checks the latest releases from GitHub
- Shows current version, latest release, and release history

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Coil
- Media3 / ExoPlayer
- Storage Access Framework
- MVVM with `ViewModel` and `StateFlow`

## Requirements

- Android 8.0+ (API 26+)
- WhatsApp or WhatsApp Business installed

## Permissions

| Permission | Purpose |
|---|---|
| `READ_MEDIA_IMAGES` / `READ_MEDIA_VIDEO` | Access status and media files |
| `READ_EXTERNAL_STORAGE` | Legacy support on older Android versions |
| `INTERNET` | Check GitHub releases |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Deleted message recovery |
| `REQUEST_INSTALL_PACKAGES` | Install downloaded updates |

## Releases

The GitHub Actions workflow automatically:

- builds a signed release APK on `main`
- creates a GitHub Release when you push a tag like `v1.0.4`
- names the APK from the tag, for example `WASaver-v1.0.4.apk`

### Required GitHub Secrets

Add these in `Settings > Secrets and variables > Actions`:

- `KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

### Create a Release

```bash
git tag -a v1.0.4 -m "Release v1.0.4"
git push origin v1.0.4
```

## Repository Rename Note

If you renamed the repository from `WASSaver-Android` to `WASaver-Android`, update your local remote:

```bash
git remote set-url origin https://github.com/LeoAristocrat/WASaver-Android.git
```

## Roadmap

- More visual polish across secondary screens
- Better onboarding for permissions
- Continued cleanup of release/update flows

---

Built by Leo Aristocrat
