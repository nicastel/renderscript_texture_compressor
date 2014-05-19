## RenderscriptTextureCompressor
RenderscriptTextureCompressor provide utility kernel in order to be able to perform texture compression directly on the device. This make easier and more performant the integration of external content coming from the World Wide Web into and Android OPENGLES application.

## License

RenderscriptTextureCompressor licensed under the [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

## Why should I use compressed texture?

The main avantages of using texture compression are the following :
- save main memory and GPU memory : no need to keep a uncompressed image into memory
- save GPU memory bandwith : the application will run faster !
- save CPU time : once the texture is in compressed format it is directly decompressed on the GPU so implements a cache somewhere where you write your

## ETC1/ETC2 texture compression format
ETC1 is a standard compression format available on all Android devices that support OPENGL ES 2.0, wich means 99,9% of android devices according to [Google](https://developer.android.com/about/dashboards/index.html?utm_source=ausdroid.net#OpenGL).

ETC2 is an enhanced version of the ETC1 format (better quality, proper support for alpha band) available on all OPENGL ES 3.0 devices (currently 13% of the Android devices).

## Why RenderScript?
Renderscript is a standard way in the Android World to parallelize code execution in order to obtain the better performance from the hardware. Trough the support library it is available to API level 8 (Android 2.2) or higher.

## Why should I use this library?
Well hopefully in the future such utility kernels will be implemented directly into the Android SDK. I have fill an enhancement request to the Android bug tracker, please vote for it : https://code.google.com/p/android/issues/detail?id=69792

In the meantime this library will provide you a faster way of compressing ETC1 texture with additionnal functionnalitys compared to the ETC1 utilities found in the Android SDK.

## Feature

- Reusable Renderscript ETC1 Block Compressor based on etc1.cpp class from Android Project (.
- PKM output : very simple file format
- DDS output : Support DDS with ETC1 compression, add the support for mipmap embedded into one single file
- DDS texture loader : Utility class to load a DDS file with ETC1 compression into OPENGLES
- PKM texture loader : Utility class to load a PKM file with ETC1 compression into OPENGLES
- Sample application with benchmarck

## Performance

The input is a 256x128 JPEG image the output a PKM with ETC1 compressed image

- Moto G, 4x Cortex A7 1.2 Ghz, Android 4.4.2 ART :                   Renderscript 36 ms, SDK 100 ms, Java  770 ms
- Moto G, 4x Cortex A7 1.2 Ghz, Android 4.4.2 DalvikVM : 
- Wiko Cink Slim, 2x CortexA9 1.0 Ghz, Android 4.1.1 DalvikVM :       Renderscript 79 ms, SDK 119 ms, Java 1278 ms
- Nexus S, 1x CortexA8 1.0 Ghz, Android 4.1.2 DalvikVM : 





