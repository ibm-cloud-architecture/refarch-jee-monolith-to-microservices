FROM registry.ng.bluemix.net/ibmliberty:latest

COPY target/ProductSearchService-0.1.war /opt/ibm/wlp/usr/servers/defaultServer/dropins/
COPY server.xml /opt/ibm/wlp/usr/servers/defaultServer/

EXPOSE 9080
EXPOSE 9443

CMD ["/opt/ibm/docker/docker-server", "run", "defaultServer"]
