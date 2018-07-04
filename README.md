# Enterprise Cloud Native for SME - Jigsaw Service

Simple Java Jigsaw demo service for the Cloud Native Night with SquareScale.

https://www.meetup.com/de-DE/cloud-native-muc/

## Building and Running

```
$ ./gradlew build
$ ./build/jlink/bin/jigsaw-service
```

## Containerizing

```
$ docker build -t cloud-native-muc-sqsc-jigsaw:1.0 .
$ docker run -it -p 9000:9000 cloud-native-muc-sqsc-jigsaw:1.0
```
