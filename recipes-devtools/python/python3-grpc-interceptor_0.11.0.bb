SUMMARY = "Simplified Python gRPC interceptors"
HOMEPAGE = "https://github.com/d5h-foss/grpc-interceptor"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=bc284fe8bece980db631f992b7b9fe9c"

SRC_URI[sha256sum] = "51b2ace8ad840e987bb69a176a3ab80a2f29a629d7e86f83d6913aef6f17a5e3"

PYPI_PACKAGE = "grpc-interceptor"

inherit pypi setuptools3

# pyproject.toml names the legacy poetry.masonry.api backend, which
# python_poetry_core can't build. The sdist ships a poetry-generated
# setup.py, so build that instead.
INSANE_SKIP += "pep517-backend"

RDEPENDS:${PN} = "python3-grpcio"
