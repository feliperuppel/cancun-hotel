FROM adoptopenjdk:11-jre-hotspot
COPY build/libs/*.jar /app/booking-api.jar
ENTRYPOINT ["java","-jar","/app/booking-api.jar"]