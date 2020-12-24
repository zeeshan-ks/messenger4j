package com.github.messenger4j.webhook.factory;

import static com.github.messenger4j.internal.gson.GsonUtil.Constants.PROP_ID;
import static com.github.messenger4j.internal.gson.GsonUtil.Constants.PROP_PAYLOAD;
import static com.github.messenger4j.internal.gson.GsonUtil.Constants.PROP_POSTBACK;
import static com.github.messenger4j.internal.gson.GsonUtil.Constants.PROP_PRIOR_MESSAGE;
import static com.github.messenger4j.internal.gson.GsonUtil.Constants.PROP_RECIPIENT;
import static com.github.messenger4j.internal.gson.GsonUtil.Constants.PROP_REFERRAL;
import static com.github.messenger4j.internal.gson.GsonUtil.Constants.PROP_SENDER;
import static com.github.messenger4j.internal.gson.GsonUtil.Constants.PROP_TIMESTAMP;
import static com.github.messenger4j.internal.gson.GsonUtil.Constants.PROP_TITLE;
import static com.github.messenger4j.internal.gson.GsonUtil.Constants.PROP_USER_REF;
import static com.github.messenger4j.internal.gson.GsonUtil.getPropertyAsInstant;
import static com.github.messenger4j.internal.gson.GsonUtil.getPropertyAsJsonObject;
import static com.github.messenger4j.internal.gson.GsonUtil.getPropertyAsString;
import static com.github.messenger4j.internal.gson.GsonUtil.hasProperty;

import com.github.messenger4j.webhook.event.PostbackEvent;
import com.github.messenger4j.webhook.event.common.PriorMessage;
import com.github.messenger4j.webhook.event.common.Referral;
import com.google.gson.JsonObject;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Max Grabenhorst
 * @since 1.0.0
 */
final class PostbackEventFactory implements BaseEventFactory<PostbackEvent> {

  private static final Logger LOG = LoggerFactory.getLogger("application");

  @Override
  public boolean isResponsible(JsonObject messagingEvent) {
    return hasProperty(messagingEvent, PROP_POSTBACK);
  }

  @Override
  public PostbackEvent createEventFromJson(JsonObject messagingEvent) {
    String senderId = "";
    try {

      senderId = getPropertyAsString(messagingEvent, PROP_SENDER, PROP_ID).get();
    } catch (Exception ex) {
      LOG.warn("senderId is null {}", senderId);
    }
    if (null == senderId || senderId.isEmpty()) {
      senderId =
          getPropertyAsString(messagingEvent, PROP_SENDER, PROP_USER_REF)
              .orElseThrow(IllegalArgumentException::new);
      LOG.warn("senderId from user_ref: {} ", senderId);
    }

    final String recipientId =
        getPropertyAsString(messagingEvent, PROP_RECIPIENT, PROP_ID)
            .orElseThrow(IllegalArgumentException::new);
    final Instant timestamp =
        getPropertyAsInstant(messagingEvent, PROP_TIMESTAMP)
            .orElseThrow(IllegalArgumentException::new);
    final String title =
        getPropertyAsString(messagingEvent, PROP_POSTBACK, PROP_TITLE)
            .orElseThrow(IllegalArgumentException::new);
    final Optional<String> payload =
        getPropertyAsString(messagingEvent, PROP_POSTBACK, PROP_PAYLOAD);
    final Optional<Referral> referral =
        getPropertyAsJsonObject(messagingEvent, PROP_POSTBACK, PROP_REFERRAL)
            .map(this::createReferralFromJson);
    final Optional<PriorMessage> priorMessage =
        getPropertyAsJsonObject(messagingEvent, PROP_PRIOR_MESSAGE)
            .map(this::getPriorMessageFromJsonObject);

    return new PostbackEvent(
        senderId, recipientId, timestamp, title, payload, referral, priorMessage);
  }
}
