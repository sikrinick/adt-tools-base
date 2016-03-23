/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.tools.lint.checks;

import static com.android.SdkConstants.ANDROID_URI;
import static com.android.SdkConstants.ATTR_SRC;
import static com.android.SdkConstants.ATTR_SRC_COMPAT;
import static com.android.SdkConstants.AUTO_URI;
import static com.android.SdkConstants.TAG_VECTOR;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.Variant;
import com.android.ide.common.repository.GradleVersion;
import com.android.ide.common.resources.ResourceUrl;
import com.android.resources.ResourceFolderType;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.LintUtils;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.ResourceXmlDetector;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.XmlContext;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Finds all the vector drawables and checks references to them in layouts.
 *
 * <p>This detector looks for common mistakes related to AppCompat support for vector drawables,
 * that is:
 * <ul>
 *     <li>Using app:srcCompat without useSupportLibrary in build.gradle
 *     <li>Using android:src with useSupportLibrary in build.gradle
 * </ul>
 */
public class VectorDrawableCompatDetector extends ResourceXmlDetector {

    /** The main issue discovered by this detector */
    public static final Issue ISSUE = Issue.create(
            "VectorDrawableCompat", //$NON-NLS-1$
            "Using VectorDrawableCompat",
            "To use VectorDrawableCompat, you need to make two modifications to your project. "
                    + "First, set `android.defaultConfig.vectorDrawables.useSupportLibrary = true` "
                    + "in your `build.gradle` file, "
                    + "and second, use `app:srcCompat` instead of `android:src` to refer to vector "
                    + "drawables.",
            Category.CORRECTNESS,
            5,
            Severity.ERROR,
            new Implementation(
                    VectorDrawableCompatDetector.class,
                    Scope.ALL_RESOURCES_SCOPE))
            .addMoreInfo("http://chris.banes.me/2016/02/25/appcompat-vector/#enabling-the-flag");

    /** Whether to skip the checks altogether. */
    private boolean mSkipChecks;

    /** All vector drawables in the project. */
    private Set<String> mVectors = Sets.newHashSet();

    /** Whether the project uses AppCompat for vectors. */
    private boolean mUseSupportLibrary;


    @Override
    public void beforeCheckProject(@NonNull Context context) {
        AndroidProject model = context.getProject().getGradleProjectModel();

        if (model == null) {
            mSkipChecks = true;
            return;
        }

        if (context.getProject().getMinSdk() >= 21) {
            mSkipChecks = true;
            return;
        }

        GradleVersion version = GradleVersion.parse(model.getModelVersion());
        if (version.getMajor() < 2) {
            mSkipChecks = true;
            return;
        }

        Variant currentVariant = context.getProject().getCurrentVariant();

        if (currentVariant == null) {
            mSkipChecks = true;
            return;
        }

        if (Boolean.TRUE.equals(
                currentVariant
                        .getMergedFlavor()
                        .getVectorDrawables()
                        .getUseSupportLibrary())) {
            mUseSupportLibrary = true;
        }
    }

    @Override
    public boolean appliesTo(@NonNull ResourceFolderType folderType) {
        return folderType == ResourceFolderType.DRAWABLE || folderType == ResourceFolderType.LAYOUT;
    }

    /**
     * Saves names of all vector resources encountered. Because "drawable" is before "layout" in
     * alphabetical order, Lint will first call this on every vector, before calling
     * {@link #visitAttribute(XmlContext, Attr)} on every attribute.
     */
    @Override
    public void visitElement(@NonNull XmlContext context, @NonNull Element element) {
        if (mSkipChecks) {
            return;
        }
        String resourceName = LintUtils.getBaseName(context.file.getName());
        mVectors.add(resourceName);
    }

    @Nullable
    @Override
    public Collection<String> getApplicableElements() {
        return Collections.singletonList(TAG_VECTOR);
    }

    @Nullable
    @Override
    public Collection<String> getApplicableAttributes() {
        return ImmutableList.of(ATTR_SRC, ATTR_SRC_COMPAT);
    }

    @Override
    public void visitAttribute(@NonNull XmlContext context, @NonNull Attr attribute) {
        if (mSkipChecks || mVectors.isEmpty()) {
            return;
        }

        String name = attribute.getLocalName();
        String namespace = attribute.getNamespaceURI();
        if ((ATTR_SRC.equals(name) && !ANDROID_URI.equals(namespace)
                || (ATTR_SRC_COMPAT.equals(name) && !AUTO_URI.equals(namespace)))) {
            // Not the attribute we are looking for.
            return;
        }

        ResourceUrl resourceUrl = ResourceUrl.parse(attribute.getValue());
        if (resourceUrl == null) {
            return;
        }

        if (mUseSupportLibrary
                && ATTR_SRC.equals(name)
                && mVectors.contains(resourceUrl.name)) {
            Location location = context.getNameLocation(attribute);
            String message = "When using VectorDrawableCompat, you need to use `app:srcCompat`.";
            context.report(ISSUE, attribute, location, message);
        }

        if (!mUseSupportLibrary
                && ATTR_SRC_COMPAT.equals(name)
                && mVectors.contains(resourceUrl.name)) {
            Location location = context.getNameLocation(attribute);
            String message = "To use VectorDrawableCompat, you need to set "
                    + "`android.defaultConfig.vectorDrawables.useSupportLibrary = true`.";
            context.report(ISSUE, attribute, location, message);
        }
    }
}