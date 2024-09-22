DESCRIPTION = "Python gRPC server"
HOMEPAGE = "https://github.com/fvasquez/python-microservices"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI = "git://github.com/fvasquez/python-microservices.git;protocol=https;branch=main"
SRCREV = "f83a51da9f826932b929d2e36c5a039b8c6598d2"

RDEPENDS_${PN} = "python3-core"

S = "${WORKDIR}/git"

do_install () {
   install -d ${D}${bindir}
   install -m 0755 ${S}/recommendations/*.py ${D}${bindir}
}

FILES_${PN} += "${bindir}/*.py"
