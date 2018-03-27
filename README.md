## Building FBReader using Android Studio and Gradle

##### Tested on

```
OS: Windows 7 x64
IDE: Android Studio 3.1 Build #AI-173.4670197, built on March 22, 2018
JRE: 1.8.0_152-release-1024-b02 amd64
JVM: OpenJDK 64-Bit Server VM by JetBrains s.r.o
NDK: 16.1.4479499
```

## Compiling

1. Clone the project (into `%userprofile%/AndroidStudioProjects` for example)

1. Import the project into Android Studio

1. Perform a project level gradle sync (CTR+SHIFT+A and search for "project sync", double click the first result). You may need to repeat it many times because of download failures. Android Studio may complain about not finding a git.exe in your path, but this will not prevent you from building the project.

1. Install Android SDK Platform API 5, 11 and 14 when prompted

1. FBReader has now been successfully built using Android Studio, but is missing the necessary binaries (.so)

## Compiling native binaries

1. To compile the binaries from the source, navigate to where you extracted the NDK

1. Execute the following command in your shell:

    ```
    ndk-build -C FBReaderJ/fBReaderJ/src/main/jni
    ```

    The binaries should be automatically placed into the appropriate directories (`FBReaderJ/fBReaderJ/src/main/lib`)

1. Finally, rebuild the project. FBReader should now run without issue on the emulator/on your device.

## Generating the introductory help epubs

This step is optional. If you would like to see the introductory help epub when FBReader starts, execute the following commands in your shell and rebuild the project:

```
cd FBReaderJ/help
generate.py proto html ../fBReaderJ/src/main/assets/data/intro
```
