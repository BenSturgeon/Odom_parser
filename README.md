## Project description
This is a project that I created as part of an internship I did in 2020 for a company called Carscan. Their product was aimed at scanning vehicles for quickly identifying damage and changes in the vehicle's condition. 

This project was intended to be an extra component which could read and identify the Odometer readings from a vehicle.

The approach taken was a 2 stage model with a tiny-yolo model trained on a small dataset for each stage. The first stage identifies an odometer quite accurately. The second stage uses the region identified as an odometer as input and runs a single shot object detection to identify the numbers in the image and then orders them into a single string. 

## Future work
If I was to continue to improve this project these would be my next milestones:

* Make the models download from the internet automatically for convenience to the user.
* Improve the algorithm for detecting the numbers, as currently it is not as accurate as I would like. I think a deep learning approach is not the best and would rather use image processing and an SVM to better identify the values.
* Add different modes for detecting so that the user can simply draw a bounding box to detect within for cases where the odometer cannot be found. 
* Improve the overall look and feel of the app.
* Provide the ability to store odometer values and link them to specific vehicles.

## Setup

| Tool      | Version |
| ---       |  ---    |
| [OpenCV](https://opencv.org) | 4.5.3
| [Android Studio](https://developer.android.com/studio) | 4.3.0
| [Android Build Tool](https://developer.android.com/about) | 29.0.3
| [Android NDK](https://developer.android.com/ndk/guides) | r21d
| [Gradle](https://gradle.org) | 7.4.1
| Ubuntu | 20.04

## To replicate this project

1. [Download and Install Android Studio](https://developer.android.com/studio)
 
2. [Install NDK, CMake and LLDB](https://developer.android.com/studio/projects/install-ndk.md)
 
3. Clone this repository as an Android Studio project :
     * In Android Studio, click on `File -> New -> Project from Version Control -> Git`
     * Paste this repository *Github URL*, choose a *project directory* and click next.
     
4. Install *OpenCV Android release* :
    * Download the latest available Android release on [OpenCV website](https://opencv.org/releases/). Currently it uses openCV 4.5.3
    * Unzip downloaded file and put **OpenCV-android-sdk** directory on a path of your choice.
 
5. Link your *Android Studio* project to the *OpenCV Android SDK* you just downloaded :
    * Open [gradle.properties](gradle.properties) file and edit following line with your own *OpenCV Android SDK* directory path :
    
          opencvsdk=/Users/Example/Downloads/OpenCV-android-sdk

6. Download model files to external storage on the device. Models can be downloaded from:  https://drive.google.com/file/d/17RR1wkNfg_uOLZmMQp3LF4wz9-siKzy4/view?usp=sharing
7.    Unzip and save the models to the external storage on your device. Adjust the paths in MainActivity.java to match your chosen directories. 
8.    Sync Gradle and run the application on your Android Device!

## Demonstration
!(demonstration 1)[https://github.com/BenSturgeon/Odom_parser/blob/master/images/Screenshot_20220323_182014_com.example.androidopencv_odometer_OCR.MainActivity.jpg?raw=true]


!(demonstration 2)[https://github.com/BenSturgeon/Odom_parser/blob/master/images/Screenshot_20220323_182144_com.example.androidopencv_odometer_OCR.MainActivity.jpg?raw=true]

!(demonstration 3)[https://github.com/BenSturgeon/Odom_parser/blob/master/images/Screenshot_20220323_182435_com.example.androidopencv_odometer_OCR.MainActivity.jpg?raw=true]


## Questions and Remarks

If you have any question or remarks regarding this project please feel free to contact me, the owner of this repo. 

## Acknowledgments
A lot of this project was inspired by the approach taken by Ivan Goncharov and his great [Youtube series](https://youtu.be/JasVghcUeyg) on applying deep learning on the edge.

The template for this project was this excellent [github repository](https://github.com/VlSomers/native-opencv-android-template)

## Keywords

Tutorial, Template, OpenCV 4.5.3, Android, Android Studio, Native, NDK, Native Development Kit, JNI, Java Native Interface, C++, Java
