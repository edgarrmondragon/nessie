/*
 * Copyright (C) 2022 Dremio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.projectnessie.gc.tool.cli.commands;

import org.projectnessie.gc.contents.LiveContentSet;
import org.projectnessie.gc.contents.LiveContentSetsRepository;
import org.projectnessie.gc.tool.cli.Closeables;
import org.projectnessie.gc.tool.cli.options.EnvironmentDefaultProvider;
import org.projectnessie.gc.tool.cli.options.IcebergOptions;
import org.projectnessie.gc.tool.cli.options.MarkOptions;
import org.projectnessie.gc.tool.cli.options.SweepOptions;
import picocli.CommandLine;

@CommandLine.Command(
    name = "gc",
    mixinStandardHelpOptions = true,
    defaultValueProvider = EnvironmentDefaultProvider.class,
    description = {
      "Run identify-live-content and expire-files + delete-orphan-files.",
      "This is the same as running a 'mark-live' + a 'sweep' command, but this variant works with the in-memory contents storage."
    })
public class MarkAndSweep extends BaseRepositoryCommand {

  @CommandLine.Mixin MarkOptions markOptions;

  @CommandLine.Mixin SweepOptions sweepOptions;

  @CommandLine.Mixin IcebergOptions icebergOptions;

  @Override
  protected void preValidate() {
    if (sweepOptions.isDeferDeletes()) {
      liveContentSetsStorageOptions.assertNotInMemory(commandSpec, "--defer-deletes");
    }
  }

  @Override
  protected Integer call(
      Closeables closeables, LiveContentSetsRepository liveContentSetsRepository) {

    LiveContentSet liveContentSet =
        identify(closeables, liveContentSetsRepository, markOptions, commandSpec);

    return expire(
        liveContentSetsRepository, liveContentSet, sweepOptions, icebergOptions, commandSpec);
  }
}
