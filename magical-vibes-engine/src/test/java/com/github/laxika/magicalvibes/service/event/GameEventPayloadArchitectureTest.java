package com.github.laxika.magicalvibes.service.event;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GameEventPayloadArchitectureTest {

    @Test
    void eventPayloadsContainNoNetworkingDtosOrMutableDomainReferences() {
        List<Class<?>> mutableDomainTypes = List.of(
                GameData.class, Card.class, Permanent.class, StackEntry.class);

        for (Class<?> payloadType : GameEventFact.class.getPermittedSubclasses()) {
            assertThat(payloadType.isRecord()).as(payloadType.getName()).isTrue();
            for (RecordComponent component : payloadType.getRecordComponents()) {
                assertThat(component.getGenericType().getTypeName())
                        .as(payloadType.getSimpleName() + "." + component.getName())
                        .doesNotContain(".networking.");
                assertThat(mutableDomainTypes)
                        .noneMatch(domainType ->
                                domainType.isAssignableFrom(component.getType()));
            }
        }

        assertThat(Arrays.stream(GameEventFact.CardSnapshot.class.getRecordComponents())
                .map(RecordComponent::getGenericType)
                .map(Object::toString)
                .toList())
                .noneMatch(type -> type.contains(".networking."));
    }
}
