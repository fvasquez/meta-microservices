# meta-microservices

Yocto demo layer for Python gRPC microservices.

This layer accompanies my talk
[Building Secure Container Images for the Cloud with Yocto](https://media.ccc.de/v/all-systems-go-2024-267-building-secure-container-images-for-the-cloud-with-yocto)
from All Systems Go! 2024.

It packages the [python-microservices](https://github.com/fvasquez/python-microservices)
demo, a bookstore made of two Python services that talk gRPC over mutual TLS:

- **recommendations**: a gRPC server that returns book recommendations by
  category. It listens on port 443.
- **marketplace**: a Flask web app on port 5000. It calls `recommendations`
  over gRPC and renders the results as an HTML page.

Each service is built as its own OCI container image. Both are bundled into a
Docker host image, and a systemd unit starts them with Docker Compose at boot.

```
  host :5000 ──► QEMU guest (microservices-host-image)
                 └─ docker compose (microservices.service)
                    ├─ marketplace      Flask, :5000 ─┐
                    └─ recommendations  gRPC,  :443 ◄─┘ mTLS
```

## Dependencies

- Yocto Project 6.0 **wrynose** (`LAYERSERIES_COMPAT = "wrynose"`)
- [openembedded-core](https://git.openembedded.org/openembedded-core)
- [meta-virtualization](https://git.yoctoproject.org/meta-virtualization), plus
  the layers it depends on: meta-openembedded (meta-oe, meta-python,
  meta-networking, meta-filesystems, meta-webserver)

The steps below fetch all of these for you.

## Build

These steps were tested in an Ubuntu 26.04 distrobox on Fedora. Any host
that wrynose supports should work.

### 1. Set up the build host

```sh
distrobox create --name wrynose --image quay.io/toolbx/ubuntu-toolbox:26.04
distrobox enter wrynose

sudo apt update
sudo apt install -y build-essential chrpath cpio debianutils diffstat file gawk gcc \
  git iputils-ping libacl1 lz4 locales python3 python3-git python3-jinja2 \
  python3-pexpect python3-pip python3-subunit socat texinfo unzip wget xz-utils zstd
sudo locale-gen en_US.UTF-8
sudo update-locale LANG=en_US.UTF-8
export LANG=en_US.UTF-8
```

### 2. Create a Poky wrynose setup

Run these from your home directory:

```sh
git clone -b 2.18 https://git.openembedded.org/bitbake
./bitbake/bin/bitbake-setup init --non-interactive \
    poky-wrynose poky-with-sstate distro/poky machine/qemux86-64
```

This creates `~/bitbake-builds/poky-wrynose/` with `layers/` and `build/`
inside. If you run `bitbake-setup init` without arguments, it asks the same
questions interactively. Pick:

- **poky-wrynose**: Poky, release 6.0
- **poky-with-sstate**: pulls prebuilt sstate from the Yocto Project CDN,
  which cuts build time a lot
- **machine/qemux86-64**
- **distro/poky**: the layer's `systemd` fragment switches the init manager,
  so you don't need `poky-altcfg`

### 3. Install and source buildtools

```sh
cd ~/bitbake-builds/poky-wrynose/build
../layers/bitbake/bin/bitbake-setup install-buildtools
```

`poky-with-sstate` uses the upstream hash equivalence server
(`wss://hashserv.yoctoproject.org/ws`), which requires Python's `websockets`
module. The Ubuntu package list above doesn't install it, so `bitbake` and
`runqemu` stop with `ModuleNotFoundError: No module named 'websockets'`.
Buildtools includes the module. Source it in **every new shell**, before
`init-build-env`:

```sh
. ~/bitbake-builds/poky-wrynose/buildtools/environment-setup-x86_64-pokysdk-linux
. ~/bitbake-builds/poky-wrynose/build/init-build-env
```

`init-build-env` works from any directory and leaves you in `build/`.

### 4. Add the layers

From `build/`:

```sh
bitbake-layers layerindex-fetch --branch wrynose --fetchdir ../layers meta-virtualization
bitbake-layers remove-layer meta-selinux
git clone https://github.com/fvasquez/meta-microservices.git ../layers/meta-microservices
bitbake-layers add-layer ../layers/meta-microservices
```

`layerindex-fetch` looks up meta-virtualization in the OpenEmbedded layer
index. It clones that layer, its dependencies and its recommended layers into
`../layers` and adds them all to `conf/bblayers.conf`. meta-selinux is only a
recommendation and this demo doesn't use it, so `remove-layer` takes it out
again.

Check the result with `bitbake-layers show-layers`:

```
layer                 path                                                         priority
===========================================================================================
core                  ~/bitbake-builds/poky-wrynose/layers/openembedded-core/meta  5
yoctobsp              ~/bitbake-builds/poky-wrynose/layers/meta-yocto/meta-yocto-bsp  5
yocto                 ~/bitbake-builds/poky-wrynose/layers/meta-yocto/meta-poky    5
openembedded-layer    ~/bitbake-builds/poky-wrynose/layers/meta-openembedded/meta-oe  5
meta-python           ~/bitbake-builds/poky-wrynose/layers/meta-openembedded/meta-python  5
networking-layer      ~/bitbake-builds/poky-wrynose/layers/meta-openembedded/meta-networking  5
filesystems-layer     ~/bitbake-builds/poky-wrynose/layers/meta-openembedded/meta-filesystems  5
webserver             ~/bitbake-builds/poky-wrynose/layers/meta-openembedded/meta-webserver  5
virtualization-layer  ~/bitbake-builds/poky-wrynose/layers/meta-virtualization     8
meta-microservices    ~/bitbake-builds/poky-wrynose/layers/meta-microservices      6
```

### 5. Enable the configuration fragments

```sh
bitbake-config-build enable-fragment meta-microservices/systemd
bitbake-config-build enable-fragment meta-microservices/container-host
```

The fragments are recorded in `conf/toolcfg.conf`. You only need to enable
them once per build directory.

### 6. Build the host image

```sh
bitbake microservices-host-image
```

This also builds the `recommendations` and `marketplace` container images,
because the host image lists them in `BUNDLED_CONTAINERS`. The
`vruntime-x86-64` multiconfig imports them into the image's Docker store as
`recommendations:latest` and `marketplace:latest`.

The result is in `tmp/deploy/images/qemux86-64/`.

## Run

Boot the image in QEMU and forward the marketplace port to the host:

```sh
QB_SLIRP_OPT="-netdev user,id=net0,hostfwd=tcp:127.0.0.1:2222-:22,hostfwd=tcp:127.0.0.1:5000-:5000" \
    runqemu microservices-host-image slirp nographic kvm snapshot qemuparams="-m 2048"
```

What each option does:

- `QB_SLIRP_OPT`: user-mode networking with no root or tap devices. It
  forwards host port 5000 to the marketplace and keeps runqemu's default
  2222→22 SSH forward. The plain `slirp` default forwards only 22 and 23.
- `snapshot`: the image ships as `.ext4.zst`, and runqemu only boots
  compressed rootfs images in snapshot mode. Changes inside the guest are
  discarded when QEMU exits.
- `kvm`: uses hardware virtualization. This needs `/dev/kvm`, which a
  distrobox passes through.
- `qemuparams="-m 2048"`: the machine default of 256 MB is too small for
  Docker plus two Python containers.
- `nographic`: puts the serial console in the terminal. Press `Ctrl-a x` to
  quit QEMU.

At boot, systemd starts `docker.service` and then `microservices.service`,
which runs `docker compose up -d` in `/services`. The console shows:

```
[  OK  ] Started Docker Application Container Engine.
         Starting Recommendations and marketplace services (Docker Compose)...
[  OK  ] Finished Recommendations and marketplace services (Docker Compose).
```

### Verify

Open <http://localhost:5000> in a browser, or run this from another terminal on the host:

```sh
curl http://localhost:5000/
```

A working setup returns a page of recommendations that marketplace fetched from
recommendations over gRPC with mTLS:

```html
<h1>Mystery books you may like</h1>
<ul>
    <li>The Maltese Falcon</li>
    <li>Murder on the Orient Express</li>
    <li>The Hound of the Baskervilles</li>
</ul>
```

## Security note

`microservices-certs` creates its CA and keys at build time, and the private
keys (`server.key`, `client.key`) are installed in the container images. This
setup is for demos only. Do not use these images or certificates in
production.

## License

MIT. See [LICENSE](LICENSE).
