SUMMARY = "Docker host image running the recommendations and marketplace containers"

require recipes-extended/images/container-image-host.bb

# Bundling the containers at build time needs vcontainer
REQUIRED_DISTRO_FEATURES:append = " vcontainer"

CONTAINER_IMAGE_HOST_EXTRA_INSTALL = "docker-compose microservices-compose"

# Imported into Docker's store as recommendations:latest and
# marketplace:latest, the names docker-compose.yaml expects.
# No :autostart: microservices.service runs docker compose.
BUNDLED_CONTAINERS = "recommendations:docker marketplace:docker"
