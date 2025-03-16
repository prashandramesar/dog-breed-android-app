# Dog Breed Classifier App

An Android application that uses machine learning to identify dog breeds from photos.
Try the app! See dog-breed-app.apk in the root folder.

## Features

- Capture photos using the device camera
- Select images from the gallery
- Identify dog breeds with confidence scores
- Clean, simple user interface

## Tech Stack

- **Android**: Native Java application
- **ML Backend**: Flask API deployed on Render
- **Image Processing**: Glide
- **Networking**: Retrofit and OkHttp
- **Image Classification**: Transfer Learning model

## Getting Started

### Prerequisites

- Android Studio 4.0+
- Android SDK 23+
- An Android device or emulator running Android 6.0 (Marshmallow) or higher

### Installation

#### Option 1: Build from source

1. Clone this repository:
   ```
   git clone https://github.com/yourusername/dog-breed-classifier.git
   ```

2. Open the project in Android Studio

3. Build and run the application on your device or emulator

#### Option 2: Install the APK

1. Download the latest APK from the [releases page](https://github.com/yourusername/dog-breed-classifier/releases)
2. Enable "Install from Unknown Sources" in your device settings
3. Install the APK by opening it on your device

## Usage

1. Launch the app
2. Select "Take Photo" to use your camera or "Choose from Gallery" to select an existing image
3. After selecting or capturing an image, press "Identify Breed"
4. View the results showing the most likely dog breeds and confidence scores

## API Backend

The app communicates with a Flask backend hosted at https://prashand.nl/predict

The backend uses a deep learning model trained to recognize over 100 different dog breeds.

## Model Information

The classification model was trained using transfer learning on a pre-trained convolutional neural network. It can identify numerous dog breeds with high accuracy.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- The dataset used for training the model was sourced from [Stanford Dogs Dataset](http://vision.stanford.edu/aditya86/ImageNetDogs/)
- Thanks to the TensorFlow and Keras teams for the deep learning framework
- Retrofit and OkHttp libraries for simplified network operations
