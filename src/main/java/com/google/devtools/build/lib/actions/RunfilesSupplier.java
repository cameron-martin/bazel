// Copyright 2015 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.lib.actions;

import com.google.common.collect.ImmutableList;
import com.google.devtools.build.lib.analysis.config.BuildConfigurationValue.RunfileSymlinksMode;
import com.google.devtools.build.lib.collect.nestedset.NestedSet;
import com.google.devtools.build.lib.vfs.PathFragment;
import java.util.Map;
import javax.annotation.Nullable;
import net.starlark.java.eval.StarlarkValue;

/** Convenience wrapper around runfiles allowing lazy expansion. */
// RunfilesSuppliers appear to be Starlark values;
// they are exposed through ctx.resolve_tools[2], for example.
public interface RunfilesSupplier extends StarlarkValue {
  /** Lazy wrapper for a single runfiles tree. */
  // TODO(bazel-team): Ideally we could refer to Runfiles objects directly here, but current package
  // structure makes this difficult. Consider moving things around to make this possible.
  interface RunfilesTree {
    /**
     * Returns the exec path of the root directory of the runfiles tree.
     *
     * <p>Should only ever be called indirectly through {@link
     * RunfilesSupplier#getExecPathForTree(RunfilesSupplier, RunfilesTree)} because {@link
     * RunfilesSupplier} may override it.
     */
    // TODO(lberki): Remove this method once RunfilesSupplier.getRunfilesTrees() is gone.
    PathFragment getPossiblyIncorrectExecPath();

    /** Returns the mapping from the location in the runfiles tree to the artifact that's there. */
    Map<PathFragment, Artifact> getMapping();

    /**
     * Returns artifacts the runfiles tree contain symlinks to.
     *
     * <p>This includes artifacts that the symlinks and root symlinks point to, not just artifacts
     * at their canonical location.
     */
    NestedSet<Artifact> getArtifacts();

    /** Returns the {@link RunfileSymlinksMode} for this runfiles tree. */
    RunfileSymlinksMode getSymlinksMode();

    /** Returns whether the runfile symlinks should be materialized during the build. */
    boolean isBuildRunfileLinks();

    /** Returns the name of the workspace that the build is occurring in. */
    String getWorkspaceName();
  }

  /** Returns the runfiles trees to be materialized on the inputs of the action. */
  ImmutableList<RunfilesTree> getRunfilesTrees();

  /**
   * If not null, the runfile tree in this runfiles supplier should be assumed to be rooted at the
   * path this method returns, regardless of the return value of {@link
   * RunfilesTree#getPossiblyIncorrectExecPath()}
   */
  @Nullable
  default PathFragment getRunfilesDirOverride() {
    return null;
  }

  /**
   * Returns the effective root for the given runfiles tree, taking the potential override directory
   * of the {@link RunfilesSupplier} into account.
   */
  static PathFragment getExecPathForTree(
      RunfilesSupplier runfilesSupplier, RunfilesTree runfilesTree) {
    if (runfilesSupplier.getRunfilesDirOverride() != null) {
      return runfilesSupplier.getRunfilesDirOverride();
    }
    return runfilesTree.getPossiblyIncorrectExecPath();
  }
}
