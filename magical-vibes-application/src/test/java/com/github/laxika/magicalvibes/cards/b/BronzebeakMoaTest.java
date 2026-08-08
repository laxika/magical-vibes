package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BronzebeakMoaTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +3/+3 until end of turn when another creature you control enters")
    void getsBoostWhenAllyCreatureEnters() {
        harness.addToBattlefield(player1, new BronzebeakMoa());
        Permanent moa = gd.playerBattlefields.get(player1.getId()).getFirst();

        castGrizzlyBears(player1);
        harness.passBothPriorities(); // resolve creature spell (triggers the Moa)
        harness.passBothPriorities(); // resolve the boost triggered ability

        assertThat(gqs.getEffectivePower(gd, moa)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, moa)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature enters")
    void noBoostWhenOpponentCreatureEnters() {
        harness.addToBattlefield(player1, new BronzebeakMoa());
        Permanent moa = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        castGrizzlyBears(player2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, moa)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, moa)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost is cumulative across multiple creature entries")
    void boostStacksForMultipleCreatures() {
        harness.addToBattlefield(player1, new BronzebeakMoa());
        Permanent moa = gd.playerBattlefields.get(player1.getId()).getFirst();

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, moa)).isEqualTo(5);

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, moa)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, moa)).isEqualTo(8);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new BronzebeakMoa());
        Permanent moa = gd.playerBattlefields.get(player1.getId()).getFirst();

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, moa)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, moa)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, moa)).isEqualTo(2);
    }

    private void castGrizzlyBears(Player player) {
        harness.setHand(player, List.of(new GrizzlyBears()));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.castCreature(player, 0);
    }
}
