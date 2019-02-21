package com.netflix.spinnaker.orca.front50.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.orca.ExecutionStatus;
import com.netflix.spinnaker.orca.Task;
import com.netflix.spinnaker.orca.TaskResult;
import com.netflix.spinnaker.orca.front50.Front50Service;
import com.netflix.spinnaker.orca.front50.model.DeliveryConfig;
import com.netflix.spinnaker.orca.pipeline.model.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit.client.Response;

import javax.annotation.Nonnull;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;

@Component
public class DeleteDeliveryConfigTask implements Task {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Front50Service front50Service;
  private ObjectMapper objectMapper;
  private DeliveryConfigUtils deliveryConfigUtils;

  @Autowired
  public DeleteDeliveryConfigTask(Front50Service front50Service, ObjectMapper objectMapper) {
    this.front50Service = front50Service;
    this.objectMapper = objectMapper;
    deliveryConfigUtils = new DeliveryConfigUtils(front50Service, objectMapper);
  }

  @Nonnull
  @Override
  public TaskResult execute(@Nonnull Stage stage) {
    if (!stage.getContext().containsKey("deliveryConfigId")) {
      throw new IllegalArgumentException("Key 'deliveryConfigId' must be provided.");
    }

    String configId = stage.getContext().get("deliveryConfigId").toString();
    Optional<DeliveryConfig> config = deliveryConfigUtils.getDeliveryConfig(configId);

    if (!config.isPresent()) {
      log.debug("Config {} does not exist, considering deletion successful.", configId);
      return new TaskResult(ExecutionStatus.SUCCEEDED);
    }

    try {
      log.debug("Deleting delivery config: " + objectMapper.writeValueAsString(config.get()));
    } catch (JsonProcessingException e) {
      log.debug("Deleting malformed delivery config:" + config.get());
    }
    Response response = front50Service.deleteDeliveryConfig(config.get().getApplication(), configId);

    ExecutionStatus taskStatus = (response.getStatus() == NO_CONTENT.value() || response.getStatus() == OK.value())
      ? ExecutionStatus.SUCCEEDED
      : ExecutionStatus.TERMINAL;

    return new TaskResult(taskStatus);
  }
}
