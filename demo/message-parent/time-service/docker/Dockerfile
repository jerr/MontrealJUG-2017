FROM openjdk:jre-alpine

ADD time-service-swarm.jar /opt/wildfly-swarm.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/opt/wildfly-swarm.jar"]
