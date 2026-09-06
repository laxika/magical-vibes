package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentOfTriggeringPlayerMayDrawCardEffect;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EachOpponentOfTriggeringPlayerMayDrawCardEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    void queuesTheChoiceForEachOpponentOfTheTriggeringPlayer() {
        Card card = createCard("Heartwood Storyteller");
        EachOpponentOfTriggeringPlayerMayDrawCardEffect effect = new EachOpponentOfTriggeringPlayerMayDrawCardEffect();
        StackEntry entry = createTriggeredEntryWithTarget(card, player1Id, List.of(effect), player2Id, UUID.randomUUID());

        resolveEffect(gd, entry, effect);

        assertThat(gd.pendingMayAbilities).singleElement().satisfies(this::assertChoice);
    }

    private void assertChoice(PendingMayAbility ability) {
        assertThat(ability.controllerId()).isEqualTo(player1Id);
        assertThat(ability.choicePlayerId()).isEqualTo(player1Id);
        assertThat(ability.targetCardId()).isEqualTo(player1Id);
        assertThat(ability.effects()).singleElement().isInstanceOf(DrawCardForTargetPlayerEffect.class);
    }
}
