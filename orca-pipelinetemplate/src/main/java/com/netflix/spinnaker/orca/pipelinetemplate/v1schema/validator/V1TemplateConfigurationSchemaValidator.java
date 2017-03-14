/*
 * Copyright 2017 Netflix, Inc.
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
package com.netflix.spinnaker.orca.pipelinetemplate.v1schema.validator;

import com.netflix.spinnaker.orca.pipelinetemplate.v1schema.model.TemplateConfiguration;
import com.netflix.spinnaker.orca.pipelinetemplate.v1schema.model.TemplateConfiguration.PipelineDefinition;
import com.netflix.spinnaker.orca.pipelinetemplate.validator.Errors;
import com.netflix.spinnaker.orca.pipelinetemplate.validator.Errors.Error;
import com.netflix.spinnaker.orca.pipelinetemplate.validator.SchemaValidator;
import com.netflix.spinnaker.orca.pipelinetemplate.validator.VersionedSchema;

public class V1TemplateConfigurationSchemaValidator implements SchemaValidator {

  private static final String SUPPORTED_VERSION = "1";

  @Override
  public void validate(VersionedSchema configuration, Errors errors) {
    if (!(configuration instanceof TemplateConfiguration)) {
      throw new IllegalArgumentException("Expected TemplateConfiguration");
    }
    TemplateConfiguration config = (TemplateConfiguration) configuration;

    if (!SUPPORTED_VERSION.equals(config.getSchemaVersion())) {
      errors.addError(Error.builder()
        .withMessage("config schema version is unsupported: expected '" + SUPPORTED_VERSION + "', got '" + config.getSchemaVersion() + "'"));
    }

    PipelineDefinition pipelineDefinition = config.getPipeline();
    if (pipelineDefinition == null) {
      errors.addError(Error.builder()
        .withMessage("Missing pipeline configuration")
        .withLocation(location("pipeline"))
      );
    } else {
      if (pipelineDefinition.getApplication() == null) {
        errors.addError(Error.builder()
          .withMessage("Missing 'application' pipeline configuration")
          .withLocation(location("pipeline.application"))
        );
      }
    }

    V1SchemaValidationHelper.validateStageDefinitions(config.getStages(), errors, V1TemplateConfigurationSchemaValidator::location);

    // TODO rz - validate required variables are set and of the correct type
  }

  private static String location(String location) {
    return "configuration:" + location;
  }
}