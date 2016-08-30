FROM openjdk:8-jre

ENV VERTICLE_FILE acq-postgres-json-fat.jar

# Set the location of the verticles
ENV VERTICLE_HOME /usr/verticles

# Copy your fat jar to the container
COPY target/$VERTICLE_FILE $VERTICLE_HOME/

# Create user/group 'folio'
RUN groupadd folio && \
    useradd -r -d $VERTICLE_HOME -g folio -M folio && \
    chown -R folio.folio $VERTICLE_HOME

# Run as this user 
USER folio

# Launch the verticle
WORKDIR $VERTICLE_HOME

# Expose this port locally in the container. 
# '6000' for access to embedded Postgres
EXPOSE 8081 6000

ENTRYPOINT ["sh", "-c"]
CMD ["java -jar $VERTICLE_FILE embed_postgres=true"]
