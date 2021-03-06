/*
 * Copyright 2017 The Bazel Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.idea.blaze.base.run.producer;

import static com.google.common.truth.Truth.assertThat;

import com.google.idea.blaze.base.command.BlazeCommandName;
import com.google.idea.blaze.base.lang.buildfile.psi.FuncallExpression;
import com.google.idea.blaze.base.lang.buildfile.psi.StringLiteral;
import com.google.idea.blaze.base.lang.buildfile.psi.util.PsiUtils;
import com.google.idea.blaze.base.model.primitives.TargetExpression;
import com.google.idea.blaze.base.model.primitives.WorkspacePath;
import com.google.idea.blaze.base.run.BlazeCommandRunConfiguration;
import com.google.idea.blaze.base.run.producers.BlazeBuildFileRunConfigurationProducer;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.psi.PsiFile;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Integration tests for {@link BlazeBuildFileRunConfigurationProducer}. */
@RunWith(JUnit4.class)
public class BlazeBuildFileRunConfigurationProducerTest
    extends BlazeRunConfigurationProducerTestCase {

  @Test
  public void testProducedFromFuncallExpression() {
    PsiFile buildFile =
        workspace.createPsiFile(
            new WorkspacePath("java/com/google/test/BUILD"), "java_test(name='unit_tests'");

    FuncallExpression target =
        PsiUtils.findFirstChildOfClassRecursive(buildFile, FuncallExpression.class);
    assertThat(target).isNotNull();

    ConfigurationContext context = createContextFromPsi(target);
    List<ConfigurationFromContext> configurations = context.getConfigurationsFromContext();
    assertThat(configurations).hasSize(1);

    ConfigurationFromContext fromContext = configurations.get(0);
    assertThat(fromContext.isProducedBy(BlazeBuildFileRunConfigurationProducer.class)).isTrue();
    assertThat(fromContext.getConfiguration()).isInstanceOf(BlazeCommandRunConfiguration.class);

    BlazeCommandRunConfiguration config =
        (BlazeCommandRunConfiguration) fromContext.getConfiguration();
    assertThat(config.getTarget())
        .isEqualTo(TargetExpression.fromString("//java/com/google/test:unit_tests"));
    assertThat(getCommandType(config)).isEqualTo(BlazeCommandName.TEST);
  }

  @Test
  public void testProducedWhenInsideFuncallExpression() {
    PsiFile buildFile =
        workspace.createPsiFile(
            new WorkspacePath("java/com/google/test/BUILD"), "java_test(name='unit_tests'");

    StringLiteral nameString =
        PsiUtils.findFirstChildOfClassRecursive(buildFile, StringLiteral.class);
    assertThat(nameString).isNotNull();

    ConfigurationContext context = createContextFromPsi(nameString);
    List<ConfigurationFromContext> configurations = context.getConfigurationsFromContext();
    assertThat(configurations).hasSize(1);

    ConfigurationFromContext fromContext = configurations.get(0);
    assertThat(fromContext.isProducedBy(BlazeBuildFileRunConfigurationProducer.class)).isTrue();
    assertThat(fromContext.getConfiguration()).isInstanceOf(BlazeCommandRunConfiguration.class);

    BlazeCommandRunConfiguration config =
        (BlazeCommandRunConfiguration) fromContext.getConfiguration();
    assertThat(config.getTarget())
        .isEqualTo(TargetExpression.fromString("//java/com/google/test:unit_tests"));
    assertThat(getCommandType(config)).isEqualTo(BlazeCommandName.TEST);
  }
}
