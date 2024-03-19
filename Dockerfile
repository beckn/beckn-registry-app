# Alpine Linux with OpenJDK JRE
# FROM alpine:latest
# RUN apk add -U tzdata curl openjdk17 && ln -s /usr/share/zoneinfo/Asia/Kolkata  /etc/localtime  && echo "Asia/Kolkata" > /etc/timezone

FROM beckn:latest 

RUN mkdir /registry registry/target registry/bin registry/tmp registry/overrideProperties
COPY tmp/registry-docker /registry/
WORKDIR /registry
RUN /usr/bin/crontab /registry/crontab.txt  

CMD ["/bin/sh" , "/registry/bin/service-start"]
