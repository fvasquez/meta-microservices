SUMMARY = "Recommendations gRPC server container image"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

require recipes-extended/images/container-app-base.bb

CONTAINER_APP = "recommendations"
CONTAINER_APP_CMD = "python3 recommendations.py"

OCI_IMAGE_CMD = ""
OCI_IMAGE_WORKINGDIR = "/services/recommendations"
OCI_IMAGE_PORTS = "443"
