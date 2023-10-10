# Alpine Linux with OpenJDK JRE
#FROM alpine:3.14
#RUN apk add -U tzdata curl openjdk11-jre-headless && ln -s /usr/share/zoneinfo/Asia/Kolkata  /etc/localtime  && echo "Asia/Kolkata" > /etc/timezone

FROM openjdk-11-headless-tz-india:latest 
EXPOSE 3000 
EXPOSE 3030

RUN mkdir /registry registry/target registry/bin registry/tmp registry/overrideProperties
COPY tmp/registry-docker /registry/
WORKDIR /registry
RUN /usr/bin/crontab /registry/crontab.txt  

CMD ["/bin/sh" , "/registry/bin/service-start"]
