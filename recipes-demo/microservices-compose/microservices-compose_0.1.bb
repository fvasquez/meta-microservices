DESCRIPTION = "Docker Compose file for the recommendations and marketplace services"
HOMEPAGE = "https://github.com/fvasquez/python-microservices"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI = "git://github.com/fvasquez/python-microservices.git;protocol=https;branch=main"
SRC_URI += "file://microservices.service"
SRCREV = "f83a51da9f826932b929d2e36c5a039b8c6598d2"

S = "${UNPACKDIR}/${BB_GIT_DEFAULT_DESTSUFFIX}"

inherit allarch systemd

SYSTEMD_SERVICE:${PN} = "microservices.service"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install () {
   install -d ${D}/services
   install -m 0644 ${S}/docker-compose.yaml ${D}/services
   install -d ${D}${systemd_system_unitdir}
   install -m 0644 ${UNPACKDIR}/microservices.service ${D}${systemd_system_unitdir}
}

FILES:${PN} = "/services/docker-compose.yaml ${systemd_system_unitdir}/microservices.service"
