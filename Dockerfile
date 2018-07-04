FROM alpine:3.7 as builder

ENV JAVA_HOME=/opt/jdk \
    PATH=${PATH}:/opt/jdk/bin \
    LANG=C.UTF-8

RUN set -ex && \
    apk add --no-cache bash && \
    wget https://download.java.net/java/early_access/alpine/19/binaries/openjdk-11-ea+19_linux-x64-musl_bin.tar.gz -O jdk.tar.gz && \
    mkdir -p /opt/jdk && \
    tar zxvf jdk.tar.gz -C /opt/jdk --strip-components=1 && \
    rm jdk.tar.gz && \
    rm /opt/jdk/lib/src.zip

ADD ./build/distributions/jigsaw-service.tar /
WORKDIR /jigsaw-service

RUN jlink --module-path lib/jigsaw-service.jar:$JAVA_HOME/jmods \
        --add-modules de.qaware.oss.cloud.service \
        --launcher jigsaw-service=de.qaware.oss.cloud.service/de.qaware.oss.cloud.service.JigsawService \
        --bind-services \
        --limit-modules java.logging,jdk.httpserver \
        --output /app \
        --verbose \
        --compress 2 \
        --strip-debug \
        --no-header-files \
        --no-man-pages

# this here creates the final image and takes the jlink output
# from the builder image
FROM alpine:3.7

COPY --from=builder /app /app

EXPOSE 9000

CMD ["/app/bin/jigsaw-service"]