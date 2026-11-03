DESCRIPTION = "Demo mTLS certificates for the recommendations and marketplace services"
HOMEPAGE = "https://github.com/fvasquez/python-microservices"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI = "git://github.com/fvasquez/python-microservices.git;protocol=https;branch=main"
SRCREV = "f83a51da9f826932b929d2e36c5a039b8c6598d2"

S = "${UNPACKDIR}/${BB_GIT_DEFAULT_DESTSUFFIX}"

DEPENDS = "openssl-native"

inherit allarch

# One CA signs both certs, so both services must come from this recipe.
# Private keys end up in the images: demo use only.
do_compile () {
   cd ${B}
   bash ${S}/gen_certs.sh
}

do_install () {
   install -d ${D}/services/recommendations ${D}/services/marketplace
   install -m 0600 ${B}/server.key ${D}/services/recommendations
   install -m 0644 ${B}/server.pem ${B}/ca.pem ${D}/services/recommendations
   install -m 0600 ${B}/client.key ${D}/services/marketplace
   install -m 0644 ${B}/client.pem ${B}/ca.pem ${D}/services/marketplace
}

PACKAGES = "recommendations-certs marketplace-certs"
FILES:recommendations-certs = "/services/recommendations"
FILES:marketplace-certs = "/services/marketplace"
