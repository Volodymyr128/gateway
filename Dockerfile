FROM java:8-jre

COPY target/gateway-1.0-SNAPSHOT.jar /opt/

EXPOSE 9669

CMD ["/usr/bin/java", "-jar", "/opt/gateway-1.0-SNAPSHOT.jar"]