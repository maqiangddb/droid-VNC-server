LOCAL_PATH := $(call my-dir)
ROOT_DIR := $(LOCAL_PATH)

include $(ROOT_DIR)/pixman.mk
include $(ROOT_DIR)/cairo.mk

################################################################
# libguac-client-vnc.so 
################################################################
include $(CLEAR_VARS)

LOCAL_MODULE    := libguac-client-vnc
LOCAL_SRC_FILES := \
    protocols/vnc/client.c                    \
    protocols/vnc/convert.c                   \
    protocols/vnc/guac_handlers.c             \
    protocols/vnc/vnc_handlers.c

LOCAL_CFLAGS    := -O2 --std=c99 -I. \
                -Ijni/vnc/LibVNCServer-0.9.9 \
                -Ijni/guacd/libguac \
                -Ijni/guacd/cairo/src \
                -Ijni/iconv/include \
                -Wno-missing-field-initializers

LOCAL_CFLAGS  +=  -Wall \
                                    -O3 \
                                    -DLIBVNCSERVER_WITH_WEBSOCKETS \
                                    -DLIBVNCSERVER_HAVE_LIBPNG \
                                    -DLIBVNCSERVER_HAVE_ZLIB \
                                    -DLIBVNCSERVER_HAVE_LIBJPEG

LOCAL_LDLIBS +=  -llog -lz -ldl 

LOCAL_STATIC_LIBRARIES := iconv
#LOCAL_STATIC_LIBRARIES := cairo iconv libssl_static libjpeg libpng
LOCAL_SHARED_LIBRARIES :=  libguac libandroidvncserver

include $(BUILD_SHARED_LIBRARY)

################################################################
#libguac
################################################################
include $(CLEAR_VARS)

LOCAL_MODULE    := libguac
LOCAL_CFLAGS    := -O2 --std=c99 -I.  \
        -Ijni/libpng \
        -Ijni/guacd/libguac \
        -Ijni/guacd/protocols/vnc \
        -Ijni/guacd/pixman/pixman -Ijni/guacd/cairo/src -Ijni/guacd/cairo-extra -Ijni/guacd/pixman-extra -Wno-missing-field-initializers
LOCAL_LDLIBS    := -ljnigraphics -lm -llog 
#-ljnigraphics
#-L /cygdrive/d/android/ndk/platforms/android-18/arch-arm/usr/lib

LOCAL_SRC_FILES := \
    libguac/audio.c           \
    libguac/client.c          \
    libguac/client-handlers.c \
    libguac/error.c           \
    libguac/hash.c            \
    libguac/instruction.c     \
    libguac/palette.c         \
    libguac/plugin.c          \
    libguac/pool.c            \
    libguac/protocol.c        \
    libguac/socket.c          \
    libguac/socket-fd.c       \
    libguac/socket-nest.c     \
    libguac/timestamp.c       \
    libguac/unicode.c         \
    libguac/wav_encoder.c    


# libguac_includes := $(ROOT_DIR)/guacd

# libguac_includes +=         \
#     $(ROOT_DIR)/libguac     \
#     $(ROOT_DIR)/libguac/guacamole

# libguac_includes :=    \
#     $(ROOT_DIR)/protocols/vnc

# libguac_includes +=     \
#    $(ROOT_DIR)/cairo/src     \
#    $(ROOT_DIR)/cairo-extra   \
#    $(ROOT_DIR)/../libpng   \
#     $(ROOT_DIR)/../iconv/include   
#    $(ROOT_DIR)/../vnc/LibVNCServer-0.9.9/rfb   \
#    $(ROOT_DIR)/../vnc/LibVNCServer-0.9.9/libvncserver   \
#    $(ROOT_DIR)/../vnc/LibVNCServer-0.9.9/common   \
#    $(ROOT_DIR)/../vnc/LibVNCServer-0.9.9

# LOCAL_C_INCLUDES := \
#     $(libguac_includes)

LOCAL_STATIC_LIBRARIES := libcairo libpixman cpufeatures libpng iconv

#LOCAL_SHARED_LIBRARIES :=  libguac-client-vnc

include $(BUILD_SHARED_LIBRARY)

################################################################
#guacd
################################################################
include $(CLEAR_VARS)

LOCAL_MODULE    := guacd

LOCAL_CFLAGS    := -O2 --std=c99 -I.  \
        -Ijni/guacd/libguac \
        -Ijni/guacd/cairo/src -Ijni/guacd/cairo-extra \
        -Wno-missing-field-initializers

LOCAL_LDLIBS    := -ljnigraphics -lm -llog 

LOCAL_SRC_FILES := \
    guacd/daemon.c  \
    guacd/client.c  \
    guacd/log.c         

LOCAL_STATIC_LIBRARIES := libcairo
LOCAL_SHARED_LIBRARIES :=  libguac

include $(BUILD_EXECUTABLE)

$(call import-module,android/cpufeatures)
