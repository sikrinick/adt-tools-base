/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tools.pixelprobe;

import com.android.tools.pixelprobe.util.Lists;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An Image represents the decoded content of an image stream.
 * The values exposed by this class depends greatly on the format
 * of the source image. For instance, an Image decoded from a JPEG
 * will not provide layers or guides, whereas an Image decoded from
 * a PSD (Photoshop) will.
 *
 * You should always check whether an Image is valid before using
 * it. Invalid images may contain erroneous or meaningless values.
 */
public class Image {
    private final int width;
    private final int height;
    private final int depth;
    private final ColorMode colorMode;
    private final ColorSpace colorSpace;

    private final float horizontalResolution;
    private final float verticalResolution;

    private final BufferedImage mergedImage;
    private final BufferedImage thumbnail;

    private final List<Guide> guides;
    private final List<Layer> layers;

    Image(Builder builder) {
        width = builder.width;
        height = builder.height;
        depth = builder.depth;
        colorMode = builder.colorMode;
        colorSpace = builder.colorSpace;

        horizontalResolution = builder.horizontalResolution;
        verticalResolution = builder.verticalResolution;

        mergedImage = builder.mergedImage;
        thumbnail = builder.thumbnail;

        guides = Lists.immutableCopy(builder.guides);
        layers = Lists.immutableCopy(builder.layers);
    }

    /**
     * Returns the width of the image in pixels.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the image in pixels.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the horizontal resolution of this image in dpi.
     */
    public float getHorizontalResolution() {
        return horizontalResolution;
    }

    /**
     * Returns the vertical resolution of this image in dpi.
     */
    public float getVerticalResolution() {
        return verticalResolution;
    }

    /**
     * Returns the depth, in bits, of each color component of this image.
     * The value will usually be 8, 16 or 32.
     */
    public int getColorDepth() {
        return depth;
    }

    /**
     * Returns the color mode of this image.
     */
    public ColorMode getColorMode() {
        return colorMode;
    }

    /**
     * Returns the color space of this image. This color space may be
     * different than the color space attached to the merged image or
     * layer images.
     */
    public ColorSpace getColorSpace() {
        return colorSpace;
    }

    /**
     * Returns a flattened (or merged or composited) version of the image
     * as a renderable image. For images without layers, this is the actual
     * image data. Make sure to check the color model and/or color space of
     * this image before using it in high performance code paths. The color
     * model/color space might not be suitable for direct rendering.
     */
    public BufferedImage getMergedImage() {
        return mergedImage;
    }

    /**
     * Returns a thumbnail for this image, if present. The returned value
     * might be null if no thumbnail was found in the original source.
     */
    public BufferedImage getThumbnailImage() {
        return thumbnail;
    }

    /**
     * Returns the list of guides for this image. The list is never null.
     */
    public List<Guide> getGuides() {
        return Collections.unmodifiableList(guides);
    }

    /**
     * Returns the list of layers for this image. The list is never null.
     */
    public List<Layer> getLayers() {
        return Collections.unmodifiableList(layers);
    }

    public static final class Builder {
        int width;
        int height;
        int depth;
        ColorMode colorMode = ColorMode.UNKNOWN;
        ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);

        float horizontalResolution = 96.0f;
        float verticalResolution = 96.0f;

        BufferedImage mergedImage;
        BufferedImage thumbnail;

        final List<Guide> guides = new ArrayList<>();
        final List<Layer> layers = new ArrayList<>();

        public int width() {
            return width;
        }

        public int height() {
            return height;
        }

        public float verticalResolution() {
            return verticalResolution;
        }

        public float horizontalResolution() {
            return horizontalResolution;
        }

        public int depth() {
            return depth;
        }

        public ColorSpace colorSpace() {
            return colorSpace;
        }

        public ColorMode colorMode() {
            return colorMode;
        }

        public Builder mergedImage(BufferedImage mergedImage) {
            this.mergedImage = mergedImage;
            return this;
        }

        public Builder dimensions(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder resolution(float horizontal, float vertical) {
            horizontalResolution = horizontal;
            verticalResolution = vertical;
            return this;
        }

        public Builder depth(int depth) {
            this.depth = depth;
            return this;
        }

        public Builder colorMode(ColorMode mode) {
            colorMode = mode;
            return this;
        }

        public Builder thumbnail(BufferedImage thumbnail) {
            this.thumbnail = thumbnail;
            return this;
        }

        public Builder addGuide(Guide guide) {
            guides.add(guide);
            return this;
        }

        public Builder addLayer(Layer layer) {
            layers.add(layer);
            return this;
        }

        public Builder colorSpace(ColorSpace colorSpace) {
            this.colorSpace = colorSpace;
            return this;
        }

        public Image build() {
            return new Image(this);
        }
    }

    @Override
    public String toString() {
        return "Image{" +
               "width=" + width +
               ", height=" + height +
               ", hRes=" + horizontalResolution +
               ", vRes=" + verticalResolution +
               ", colorMode=" + colorMode +
               ", guides=" + guides.size() +
               ", layers=" + layers.size() +
               ", hasThumbnail=" + (thumbnail != null) +
               '}';
    }
}
