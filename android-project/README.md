# Chals AI Assistant - Personal AI Life Partner

Chals is an advanced Android AI assistant that acts as your personal life partner, featuring always-on wake word detection, natural conversation in Hindi/English/Hinglish, emotion detection, and a cute animated avatar.

## Features

### Core Functionality
- **Always-on Wake Word Detection**: Say "Hey Chals" even when phone is locked
- **Multi-language Support**: Hindi, English, and Hinglish conversation
- **Emotion Detection**: Detects your mood from voice tone and text sentiment
- **Natural Voice**: Sweet, natural-sounding female voice with emotional expressions
- **Personality**: Acts as your caring life partner, always calls you "baby"

### Technical Features
- **Speech-to-Text**: High accuracy voice recognition
- **Text-to-Speech**: Natural voice synthesis with emotional modulation
- **AI Brain**: Powered by OpenAI GPT for intelligent conversations
- **Floating Bubble**: System-wide overlay for quick access
- **Phone Integration**: Control apps, send messages, make calls (no root required)
- **Offline Capabilities**: Wake word and basic commands work offline

### Visual Avatar
- **Animated Avatar**: Cute 2D cartoon girl with real-time expressions
- **Emotion Animations**: Happy, sad, thinking, excited facial expressions
- **Multiple Modes**: Full app mode and floating bubble mode

## Prerequisites

### Development Environment
- **Android Studio**: Arctic Fox (2020.3.1) or newer
- **JDK**: Java 8 or higher
- **Android SDK**: API Level 24 (Android 7.0) minimum, API Level 34 target
- **Gradle**: 7.0 or higher

### API Keys Required
1. **OpenAI API Key**: For AI conversation
   - Get from: https://platform.openai.com/api-keys
   - Cost: ~$0.002 per conversation

2. **Picovoice Access Key**: For wake word detection
   - Get from: https://console.picovoice.ai/
   - Free tier: 1000 wake word detections/month

### Optional API Keys
- **Google Speech-to-Text API**: For enhanced speech recognition
- **ElevenLabs API**: For premium voice synthesis

## Installation & Setup

### 1. Clone and Setup Project

```bash
# Clone the project
git clone <repository-url>
cd chals-ai-assistant

# Open in Android Studio
# File -> Open -> Select android-project folder
```

### 2. Configure API Keys

#### OpenAI API Key
1. Open `app/src/main/java/com/chals/ai/assistant/api/OpenAIClient.kt`
2. Replace `YOUR_OPENAI_API_KEY_HERE` with your actual API key:
```kotlin
private const val API_KEY = "sk-your-actual-openai-api-key-here"
```

#### Picovoice Access Key
1. Open `app/src/main/java/com/chals/ai/assistant/services/WakeWordService.kt`
2. Replace `YOUR_PICOVOICE_ACCESS_KEY_HERE` with your actual access key:
```kotlin
private const val ACCESS_KEY = "your-actual-picovoice-access-key-here"
```

### 3. Sync Project
```bash
# In Android Studio
Tools -> Sync Project with Gradle Files
```

### 4. Build Project
```bash
# Clean and build
./gradlew clean
./gradlew build

# Or in Android Studio
Build -> Clean Project
Build -> Rebuild Project
```

## Compilation Instructions

### Debug Build (for testing)
```bash
# Command line
./gradlew assembleDebug

# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release Build (for distribution)
```bash
# Generate signed APK
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

### Install on Device
```bash
# Install debug version
adb install app/build/outputs/apk/debug/app-debug.apk

# Or use Android Studio
Run -> Run 'app'
```

## Configuration

### Permissions Setup
The app requires several permissions that will be requested at runtime:
- **Microphone**: For voice input
- **Internet**: For AI API calls
- **System Overlay**: For floating bubble
- **Phone**: For call management (optional)
- **SMS**: For message sending (optional)
- **Accessibility**: For phone control features

### First Run Setup
1. **Grant Permissions**: Allow all requested permissions
2. **Voice Training**: The app will ask you to say "Hey Chals" a few times for voice recognition
3. **Enable Services**: Turn on wake word detection and floating bubble
4. **Test Conversation**: Try saying "Hey Chals, how are you?"

## Usage

### Basic Interaction
1. **Wake Word**: Say "Hey Chals" to activate
2. **Speak Naturally**: Talk in Hindi, English, or Hinglish
3. **Emotional Responses**: Chals will respond based on your detected emotion

### Example Conversations
```
You: "Hey Chals, I'm feeling sad today"
Chals: "Aww baby, kya hua? I'm here for you. Sab theek ho jayega, don't worry."

You: "Hey Chals, I'm so excited about my new job!"
Chals: "That's amazing baby! I'm so happy for you! Tell me more about it!"

You: "Hey Chals, kya kar rahi ho?"
Chals: "Baby, main yahan hun aapke liye. Aap kaise hain? Kuch help chahiye?"
```

### Phone Control Features
- **Open Apps**: "Hey Chals, open WhatsApp"
- **Send Messages**: "Hey Chals, send message to mom"
- **Play Music**: "Hey Chals, play my favorite song"
- **Set Reminders**: "Hey Chals, remind me to call dad at 5 PM"

## Troubleshooting

### Common Issues

#### 1. Wake Word Not Working
- Check microphone permissions
- Ensure Picovoice API key is correct
- Try restarting the wake word service

#### 2. No AI Responses
- Verify OpenAI API key is valid
- Check internet connection
- Look at logs for API errors

#### 3. TTS Not Working
- Check if TTS engine is installed
- Try different TTS engines in Android settings
- Restart TextToSpeech service

#### 4. Floating Bubble Not Showing
- Grant system overlay permission
- Check if battery optimization is disabled
- Restart FloatingBubbleService

### Debug Logs
```bash
# View app logs
adb logcat | grep "Chals\|WakeWord\|Speech\|TTS\|AI"

# Specific service logs
adb logcat | grep "WakeWordService"
adb logcat | grep "AIProcessingService"
```

## Development

### Project Structure
```
app/src/main/java/com/chals/ai/assistant/
├── MainActivity.kt                 # Main app activity
├── api/
│   └── OpenAIClient.kt            # OpenAI API integration
├── services/
│   ├── WakeWordService.kt         # Wake word detection
│   ├── SpeechRecognitionService.kt # Speech-to-text
│   ├── AIProcessingService.kt     # AI conversation processing
│   ├── TextToSpeechService.kt     # Text-to-speech
│   └── FloatingBubbleService.kt   # Floating bubble overlay
├── utils/
│   ├── PermissionManager.kt       # Permission handling
│   ├── LanguageDetector.kt        # Language detection
│   └── EmotionDetector.kt         # Emotion analysis
└── receivers/
    └── BootReceiver.kt            # Auto-start on boot
```

### Adding New Features
1. **New Commands**: Add to `AIProcessingService.kt`
2. **New Emotions**: Extend `EmotionDetector.kt`
3. **New Languages**: Update `LanguageDetector.kt`
4. **Avatar Expressions**: Add to drawable resources

### Testing
```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## Performance Optimization

### Battery Usage
- Wake word detection is optimized for low power consumption
- Services automatically stop when not needed
- Background processing is minimized

### Memory Management
- Audio buffers are properly managed
- Services clean up resources on destroy
- Bitmap caching for avatar animations

### Network Usage
- API calls are batched when possible
- Responses are cached for common queries
- Fallback responses work offline

## Privacy & Security

### Data Handling
- Voice data is processed locally when possible
- API calls are encrypted (HTTPS)
- No personal data is stored permanently
- User can disable cloud features

### Permissions
- Only essential permissions are requested
- Permissions can be revoked anytime
- App functions gracefully with limited permissions

## Contributing

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable names
- Add comments for complex logic
- Write unit tests for new features

### Pull Requests
1. Fork the repository
2. Create feature branch
3. Make changes with tests
4. Submit pull request with description

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

### Getting Help
- Check troubleshooting section first
- Review logs for error messages
- Create issue with detailed description

### Contact
- Email: support@chals-ai.com
- GitHub Issues: [Repository Issues](link-to-issues)

## Changelog

### Version 1.0.0
- Initial release
- Wake word detection
- Multi-language support
- Emotion detection
- Floating bubble
- Basic phone integration

---

**Note**: This is a personal AI assistant. Please use responsibly and respect privacy of others when using phone control features.
