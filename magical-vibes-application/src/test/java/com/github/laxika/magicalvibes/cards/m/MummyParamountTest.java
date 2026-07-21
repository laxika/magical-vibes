package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MummyParamountTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 until end of turn when another Zombie enters")
    void getsBoostWhenZombieEnters() {
        harness.addToBattlefield(player1, new MummyParamount());
        Permanent paramount = gd.playerBattlefields.get(player1.getId()).getFirst();

        castScatheZombies(player1);
        harness.passBothPriorities(); // resolve creature spell (triggers Mummy Paramount)
        harness.passBothPriorities(); // resolve Mummy Paramount's boost triggered ability

        assertThat(gqs.getEffectivePower(gd, paramount)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, paramount)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger when a non-Zombie creature enters")
    void noBoostWhenNonZombieEnters() {
        harness.addToBattlefield(player1, new MummyParamount());
        Permanent paramount = gd.playerBattlefields.get(player1.getId()).getFirst();

        // Cast Grizzly Bears (Bear, not a Zombie)
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gqs.getEffectivePower(gd, paramount)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, paramount)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's Zombie enters")
    void noBoostWhenOpponentZombieEnters() {
        harness.addToBattlefield(player1, new MummyParamount());
        Permanent paramount = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        castScatheZombies(player2);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gqs.getEffectivePower(gd, paramount)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, paramount)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost is cumulative across multiple Zombie entries")
    void boostStacksForMultipleZombies() {
        harness.addToBattlefield(player1, new MummyParamount());
        Permanent paramount = gd.playerBattlefields.get(player1.getId()).getFirst();

        castScatheZombies(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paramount)).isEqualTo(3);

        castScatheZombies(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paramount)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, paramount)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new MummyParamount());
        Permanent paramount = gd.playerBattlefields.get(player1.getId()).getFirst();

        castScatheZombies(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paramount)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paramount)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, paramount)).isEqualTo(2);
    }

    private void castScatheZombies(Player player) {
        harness.setHand(player, List.of(new ScatheZombies()));
        harness.addMana(player, ManaColor.BLACK, 3);
        harness.castCreature(player, 0);
    }
}
