DESCRIPTION = "Python gRPC server"
HOMEPAGE = "https://github.com/fvasquez/python-microservices"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI = "git://github.com/fvasquez/python-microservices.git;protocol=https;branch=main"
SRCREV = "f83a51da9f826932b929d2e36c5a039b8c6598d2"

RDEPENDS:${PN} = "python3-core python3-grpcio python3-protobuf"

S = "${UNPACKDIR}/${BB_GIT_DEFAULT_DESTSUFFIX}"

DEPENDS = "python3-grpcio-tools-native"

inherit python3native

do_compile () {
   ${PYTHON} -m grpc_tools.protoc -I${S}/protobufs --python_out=${B} \
       --grpc_python_out=${B} ${S}/protobufs/recommendations.proto
}

do_install () {
   install -d ${D}${bindir}
   install -m 0755 ${S}/recommendations/*.py ${D}${bindir}
   install -m 0644 ${B}/recommendations_pb2.py ${B}/recommendations_pb2_grpc.py ${D}${bindir}
}

FILES:${PN} += "${bindir}/*.py"
