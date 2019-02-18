# Version 0.0.1
FROM java:latest
MAINTAINER cong "yhalben@163.com"
ENV JAVA_SRC_DIR /home/cong/java/src
WORKDIR /home
RUN mkdir -p cong/java/src
WORKDIR $JAVA_SRC_DIR
ADD EchoNIOServer.java $JAVA_SRC_DIR/EchoNIOServer.java
RUN javac EchoNIOServer.java
EXPOSE 8080
