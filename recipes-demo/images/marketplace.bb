SUMMARY = "Marketplace gRPC client container image"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

require recipes-extended/images/container-app-base.bb

CONTAINER_APP = "marketplace-app"
CONTAINER_APP_CMD = "python3 -m flask run --host=0.0.0.0"

OCI_IMAGE_CMD = ""
OCI_IMAGE_WORKINGDIR = "/services/marketplace"
OCI_IMAGE_PORTS = "5000"
OCI_IMAGE_ENV_VARS = "FLASK_APP=marketplace.py RECOMMENDATIONS_HOST=recommendations"
