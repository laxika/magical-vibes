package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.testutil.TestStackDoor;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link CopySupport#createCopyStackEntry} for a copied ability: the copy is another instance of the same ability, so
 * it reads the same trigger event as the original (CR 707.10).
 */
class CopySupportAbilityCopyTest {

    private final CopySupport copySupport = new CopySupport();

    @Test
    @DisplayName("A copied triggered ability keeps the trigger event the original carries")
    void copiedTriggeredAbilityKeepsTheTriggerEvent() {
        Card source = card("The Ozolith");
        Permanent departed = new Permanent(card("Grizzly Bears"));
        departed.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Card discarded = card("Forest");
        UUID player = UUID.randomUUID();
        StackEntry original = TestStackDoor.triggered(source, UUID.randomUUID(),
                "The Ozolith's ability", List.of(), null, UUID.randomUUID());
        original.setTriggeringPermanent(departed);
        original.setTriggeringPermanentId(departed.getId());
        original.setTriggeringCards(List.of(discarded));
        original.setTriggeringPlayerId(player);
        original.setEventValue(3);

        StackEntry copy = copySupport.createCopyStackEntry(original, copySupport.createCopyCard(source),
                original.getControllerId(), original.getTargetId());

        assertThat(copy.isCopy()).isTrue();
        assertThat(copy.getTriggeringPermanentSnapshot()).isNotNull()
                .isNotSameAs(original.getTriggeringPermanentSnapshot());
        assertThat(copy.getTriggeringPermanentSnapshot().getId()).isEqualTo(departed.getId());
        assertThat(copy.triggeringPermanentCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(copy.getTriggeringPermanentId()).isEqualTo(departed.getId());
        assertThat(copy.getTriggeringCards()).containsExactly(discarded);
        assertThat(copy.getTriggeringPlayerId()).isEqualTo(player);
        assertThat(copy.getEventValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("A copied activated ability keeps what its entry recorded about a trigger event")
    void copiedActivatedAbilityKeepsTheTriggerEvent() {
        Card source = card("Hangarback Walker");
        UUID player = UUID.randomUUID();
        StackEntry original = new StackEntry(StackEntryType.ACTIVATED_ABILITY, source, UUID.randomUUID(),
                "Hangarback Walker's ability", List.of(), null, UUID.randomUUID());
        original.setTriggeringPlayerId(player);

        StackEntry copy = copySupport.createCopyStackEntry(original, copySupport.createCopyCard(source),
                original.getControllerId(), original.getTargetId());

        assertThat(copy.getTriggeringPlayerId()).isEqualTo(player);
    }

    private static Card card(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
