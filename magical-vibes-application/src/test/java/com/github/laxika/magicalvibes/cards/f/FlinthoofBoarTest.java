package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlinthoofBoarTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 while controller controls a Mountain")
    void boostedWithMountain() {
        Permanent boar = addCreatureReady(player1, new FlinthoofBoar());
        harness.addToBattlefield(player1, new Mountain());

        assertThat(gqs.getEffectivePower(gd, boar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, boar)).isEqualTo(3);
    }

    @Test
    @DisplayName("No boost without a Mountain")
    void noBoostWithoutMountain() {
        Permanent boar = addCreatureReady(player1, new FlinthoofBoar());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, boar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, boar)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's Mountain does not grant the boost")
    void opponentMountainDoesNotBoost() {
        Permanent boar = addCreatureReady(player1, new FlinthoofBoar());
        harness.addToBattlefield(player2, new Mountain());

        assertThat(gqs.getEffectivePower(gd, boar)).isEqualTo(2);
    }

    @Test
    @DisplayName("Paying {R} grants haste until end of turn")
    void payingRedGrantsHaste() {
        Permanent boar = addCreatureReady(player1, new FlinthoofBoar());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(boar.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Haste wears off at end of turn")
    void hasteWearsOffAtEndOfTurn() {
        Permanent boar = addCreatureReady(player1, new FlinthoofBoar());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(boar.getGrantedKeywords()).contains(Keyword.HASTE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(boar.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }
}
