package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.MeliraSylvokOutcast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaressOfPhyrexia.class, MeliraSylvokOutcast.class})
class CaressOfPhyrexiaTest extends BaseCardTest {

    

    @Test
    @DisplayName("Target player draws three cards, loses 3 life, and gets three poison counters")
    void resolvesAllEffectsOnOpponent() {
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        castCaressTargeting(player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        castCaressTargeting(player1.getId());

        // setHand sets hand to [Caress], casting removes it (0), then draws 3
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("Goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        castCaressTargeting(player2.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Caress of Phyrexia");
    }

    @Test
    @DisplayName("Does not affect non-targeted player")
    void doesNotAffectNonTargetedPlayer() {
        castCaressTargeting(player2.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Poison counters are added to existing counters")
    void addsToExistingPoisonCounters() {
        gd.playerPoisonCounters.put(player2.getId(), 4);

        castCaressTargeting(player2.getId());

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(7);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Melira prevents poison but not drawing cards or losing life")
    void poisonPreventionDoesNotPreventOtherEffects() {
        harness.addToBattlefield(player2, new MeliraSylvokOutcast());
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        castCaressTargeting(player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 3);
        harness.assertLife(player2, 17);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.assertInGraveyard(player1, "Caress of Phyrexia");
    }
    private void castCaressTargeting(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new CaressOfPhyrexia()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }
}
