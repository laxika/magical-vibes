package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.networking.message.ActivateAbilityRequest;
import com.github.laxika.magicalvibes.networking.message.TapPermanentRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Both request records grew an optional {@code paymentIntent} alongside a convenience constructor
 * that omits it. Jackson binds these records straight off the wire, so this pins that the extra
 * constructor did not displace the canonical one — a silent mis-bind would drop the intent and turn
 * the colour narrowing into a no-op nobody notices.
 */
class ManaPaymentIntentWireTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("A tap request carries the cast it is paying for")
    void tapRequestBindsCastIntent() {
        String json = """
                {"permanentIndex":2,"paymentIntent":{"handCardIndex":3,"xValue":0}}""";

        TapPermanentRequest request = objectMapper.readValue(json, TapPermanentRequest.class);

        assertThat(request.permanentIndex()).isEqualTo(2);
        assertThat(request.paymentIntent()).isNotNull();
        assertThat(request.paymentIntent().isCast()).isTrue();
        assertThat(request.paymentIntent().handCardIndex()).isEqualTo(3);
    }

    @Test
    @DisplayName("An activation request carries the ability it is paying for")
    void activateRequestBindsAbilityIntent() {
        String json = """
                {"permanentIndex":1,"abilityIndex":0,"paymentIntent":\
                {"abilityPermanentId":"11111111-2222-3333-4444-555555555555","abilityIndex":1}}""";

        ActivateAbilityRequest request = objectMapper.readValue(json, ActivateAbilityRequest.class);

        assertThat(request.permanentIndex()).isEqualTo(1);
        assertThat(request.paymentIntent()).isNotNull();
        assertThat(request.paymentIntent().isAbility()).isTrue();
        assertThat(request.paymentIntent().abilityIndex()).isEqualTo(1);
    }

    @Test
    @DisplayName("Requests without an intent still bind")
    void bindsWithoutIntent() {
        assertThat(objectMapper.readValue("{\"permanentIndex\":4}", TapPermanentRequest.class).paymentIntent())
                .isNull();
        assertThat(objectMapper.readValue("{\"permanentIndex\":4,\"abilityIndex\":0}",
                ActivateAbilityRequest.class).paymentIntent()).isNull();
    }
}
