package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CaressOfPhyrexiaTest extends BaseCardTest {

    @Test
    @DisplayName("Has correct effects and targeting")
    void hasCorrectProperties() {
        CaressOfPhyrexia card = new CaressOfPhyrexia();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getTargetFilter()).isInstanceOf(PlayerPredicateTargetFilter.class);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(3);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DrawCardForTargetPlayerEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(TargetPlayerLosesLifeEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(2)).isInstanceOf(GiveTargetPlayerPoisonCountersEffect.class);
    }

    @Test
    @DisplayName("Target player draws three cards, loses 3 life, and gets three poison counters")
    void resolvesAllEffectsOnOpponent() {
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        castCaressTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        castCaressTargeting(player1.getId());
        harness.passBothPriorities();

        // setHand sets hand to [Caress], casting removes it (0), then draws 3
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("Goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        castCaressTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Caress of Phyrexia");
    }

    @Test
    @DisplayName("Does not affect non-targeted player")
    void doesNotAffectNonTargetedPlayer() {
        castCaressTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }

    private void castCaressTargeting(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new CaressOfPhyrexia()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, targetPlayerId);
    }
}
