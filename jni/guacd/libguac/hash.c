
/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is libguac.
 *
 * The Initial Developer of the Original Code is
 * Michael Jumper.
 * Portions created by the Initial Developer are Copyright (C) 2010
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */


#include <stdint.h>
#include <string.h>
#include "cairo.h"

/*
 * Arbitrary hash function whhich maps ALL 32-bit numbers onto 24-bit numbers
 * evenly, while guaranteeing that all 24-bit numbers are mapped onto
 * themselves.
 */
unsigned int _guac_hash_32to24(unsigned int value) {

    /* Grab highest-order byte */
    unsigned int upper = value & 0xFF000000;

    /* XOR upper with lower three bytes, truncate to 24-bit */
    return
          (value & 0xFFFFFF)
        ^ (upper >> 8)
        ^ (upper >> 16)
        ^ (upper >> 24);

}

/**
 * Rotates a given 32-bit integer by N bits.
 *
 * NOTE: We probably should check for available bitops.h macros first.
 */
unsigned int _guac_rotate(unsigned int value, int amount) {

    /* amount = amount % 32 */
    amount &= 0x1F; 

    /* Return rotated amount */
    return (value >> amount) | (value << (32 - amount));

}

unsigned int guac_hash_surface(cairo_surface_t* surface) {

    /* Init to zero */
    unsigned int hash_value = 0;

    int x, y;

    /* Get image data and metrics */
    unsigned char* data = cairo_image_surface_get_data(surface);
    int width = cairo_image_surface_get_width(surface);
    int height = cairo_image_surface_get_height(surface);
    int stride = cairo_image_surface_get_stride(surface);

    for (y=0; y<height; y++) {

        /* Get current row */
        uint32_t* row = (uint32_t*) data;
        data += stride;

        for (x=0; x<width; x++) {

            /* Get color at current pixel */
            unsigned int color = *row;
            row++;

            /* Compute next hash */
            hash_value =
                _guac_rotate(hash_value, 1) ^ color ^ 0x1B872E69;

        }

    } /* end for each row */

    /* Done */
    return _guac_hash_32to24(hash_value);

}

int guac_surface_cmp(cairo_surface_t* a, cairo_surface_t* b) {

    /* Surface A metrics */
    unsigned char* data_a = cairo_image_surface_get_data(a);
    int width_a = cairo_image_surface_get_width(a);
    int height_a = cairo_image_surface_get_height(a);
    int stride_a = cairo_image_surface_get_stride(a);

    /* Surface B metrics */
    unsigned char* data_b = cairo_image_surface_get_data(b);
    int width_b = cairo_image_surface_get_width(b);
    int height_b = cairo_image_surface_get_height(b);
    int stride_b = cairo_image_surface_get_stride(b);

    int y;

    /* If core dimensions differ, just compare those. Done. */
    if (width_a != width_b) return width_a - width_b;
    if (height_a != height_b) return height_a - height_b;

    for (y=0; y<height_a; y++) {

        /* Compare row. If different, use that result. */
        int cmp_result = memcmp(data_a, data_b, width_a * 4);
        if (cmp_result != 0)
            return cmp_result;

        /* Next row */
        data_a += stride_a;
        data_b += stride_b;

    }

    /* Otherwise, same. */
    return 0;

}
