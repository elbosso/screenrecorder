# screenrecorder

<!---
[![start with why](https://img.shields.io/badge/start%20with-why%3F-brightgreen.svg?style=flat)](http://www.ted.com/talks/simon_sinek_how_great_leaders_inspire_action)
--->
[![GitHub release](https://img.shields.io/github/release/elbosso/screenrecorder/all.svg?maxAge=1)](https://GitHub.com/elbosso/screenrecorder/releases/)
[![GitHub tag](https://img.shields.io/github/tag/elbosso/screenrecorder.svg)](https://GitHub.com/elbosso/screenrecorder/tags/)
[![GitHub license](https://img.shields.io/github/license/elbosso/screenrecorder.svg)](https://github.com/elbosso/screenrecorder/blob/master/LICENSE)
[![GitHub issues](https://img.shields.io/github/issues/elbosso/screenrecorder.svg)](https://GitHub.com/elbosso/screenrecorder/issues/)
[![GitHub issues-closed](https://img.shields.io/github/issues-closed/elbosso/screenrecorder.svg)](https://GitHub.com/elbosso/screenrecorder/issues?q=is%3Aissue+is%3Aclosed)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/elbosso/screenrecorder/issues)
[![GitHub contributors](https://img.shields.io/github/contributors/elbosso/screenrecorder.svg)](https://GitHub.com/elbosso/screenrecorder/graphs/contributors/)
[![Github All Releases](https://img.shields.io/github/downloads/elbosso/screenrecorder/total.svg)](https://github.com/elbosso/screenrecorder)
[![Website elbosso.github.io](https://img.shields.io/website-up-down-green-red/https/elbosso.github.io.svg)](https://elbosso.github.io/)

## Overview

This project offers a screencast recorder that outputs avi files and a screenshot tool that saves
png files. It opens a red frame that can be freely placed anywhere on the Desktop and resized and defines the area that
is going to reccorded.

It started out as a training exercise to get familiar with the Java Media Framework and while I was at it I wanted to find out if
one could make the central feature of any such software with java: the ui for choosing a viewport on the desktop. I know there are 
many more solutions (maybe better ones too) for doing this - I for example use avconv:

```
avconv -f x11grab -show_region 1 -follow_mouse 100 -r 10\\
-s 960x540 -i :0.0+10,200 -acodec pcm_s16le\\
-qscale 0  -vcodec libx264 /tmp/aviator_scale.mp4
```

But as I said - it offered the opportunity to learn something and i seldom say no when it happens...

You can build it by issuing

```
mvn compile package
```

and then starting the resulting monolithic jar file by issuing

```
$JAVA_HOME/bin/java -jar target/screenrecorder-<version>-jar-with-dependencies.jar
```

Alternatively one could just start the app using maven by  issuing

```
mvn compile exec:java
```


