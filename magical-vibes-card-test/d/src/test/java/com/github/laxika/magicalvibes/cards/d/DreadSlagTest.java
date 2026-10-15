package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreadSlag.class, MistralCharger.class})
class DreadSlagTest extends BaseCardTest {

    @Test
    @DisplayName("Has its printed power and toughness with an empty controller hand")
    void fullSizeWithEmptyControllerHand() {
        harness.setHand(player1, List.of());
        Permanent dreadSlag = addDreadSlag(player1);

        assertThat(gqs.getEffectivePower(gd, dreadSlag)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, dreadSlag)).isEqualTo(9);
    }

    @Test
    @DisplayName("Gets -4/-4 for each card in its controller's hand")
    void shrinksWithControllerHandSize() {
        harness.setHand(player1, handOf(2));
        Permanent dreadSlag = addDreadSlag(player1);

        assertThat(gqs.getEffectivePower(gd, dreadSlag)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, dreadSlag)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts only its controller's hand")
    void ignoresOpponentsHand() {
        harness.setHand(player1, handOf(1));
        harness.setHand(player2, handOf(4));
        Permanent dreadSlag = addDreadSlag(player1);

        assertThat(gqs.getEffectivePower(gd, dreadSlag)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dreadSlag)).isEqualTo(5);
    }

    @Test
    @DisplayName("Updates dynamically as its controller's hand changes")
    void updatesDynamically() {
        harness.setHand(player1, handOf(1));
        Permanent dreadSlag = addDreadSlag(player1);
        assertThat(gqs.getEffectiveToughness(gd, dreadSlag)).isEqualTo(5);

        gd.playerHands.get(player1.getId()).add(new MistralCharger());
        assertThat(gqs.getEffectiveToughness(gd, dreadSlag)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dies when three cards reduce its toughness below zero")
    void diesWhenReducedToZeroToughness() {
        harness.setHand(player1, handOf(3));
        addDreadSlag(player1);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Dread Slag");
        harness.assertInGraveyard(player1, "Dread Slag");
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setHand(player1, List.of());
        harness.setLife(player2, 20);

        addCreatureReady(player1, new DreadSlag());
        Permanent blocker = addCreatureReady(player2, new MistralCharger());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 8));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        harness.assertInGraveyard(player2, "Mistral Charger");
        harness.assertOnBattlefield(player1, "Dread Slag");
    }

    @Test
    @DisplayName("Grows immediately when its controller's hand becomes empty")
    void growsWhenHandEmpties() {
        harness.setHand(player1, handOf(2));
        Permanent dreadSlag = addDreadSlag(player1);
        assertThat(gqs.getEffectivePower(gd, dreadSlag)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, dreadSlag)).isEqualTo(1);

        harness.setHand(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, dreadSlag)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, dreadSlag)).isEqualTo(9);
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player1, "Dread Slag");
    }

    @Test
    @DisplayName("Casting Dread Slag removes it from the hand before its static penalty applies")
    void castingWithNoOtherCardsLeavesNoPenalty() {
        harness.castFromHand(player1, new DreadSlag(), "{3}{B}{R}");
        harness.passBothPriorities();

        Permanent dreadSlag = findPermanent(player1, "Dread Slag");
        assertThat(gqs.getEffectivePower(gd, dreadSlag)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, dreadSlag)).isEqualTo(9);
        harness.assertOnBattlefield(player1, "Dread Slag");
    }
    private Permanent addDreadSlag(Player player) {
        return harness.addToBattlefieldAndReturn(player, new DreadSlag());
    }

    private List<Card> handOf(int count) {
        return new ArrayList<>(IntStream.range(0, count)
                .mapToObj(i -> (Card) new MistralCharger())
                .toList());
    }
}
