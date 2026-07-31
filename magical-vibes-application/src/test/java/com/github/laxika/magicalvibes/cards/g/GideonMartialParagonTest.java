package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GideonMartialParagonTest extends BaseCardTest {

    @Test
    @DisplayName("+2 untaps controlled creatures and gives them +1/+1 until end of turn")
    void plusTwoUntapsAndPumps() {
        Permanent gideon = addReadyGideon(player1, 5);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.tap();
        Permanent opp = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opp)).isEqualTo(2);
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    @DisplayName("+2 boost wears off at end of turn")
    void plusTwoBoostWearsOff() {
        addReadyGideon(player1, 5);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("0 animates Gideon into a 5/5 indestructible creature")
    void zeroAnimatesIntoIndestructibleCreature() {
        Permanent gideon = addReadyGideon(player1, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, gideon)).isTrue();
        assertThat(gqs.getEffectivePower(gd, gideon)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, gideon)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, gideon, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("0 prevents all damage dealt to Gideon this turn (no loyalty loss)")
    void zeroPreventsDamageToGideon() {
        Permanent gideon = addReadyGideon(player1, 5);
        int loyaltyBefore = gideon.getCounterCount(CounterType.LOYALTY);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, gideon.getId());
        harness.passBothPriorities();

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore);
    }

    @Test
    @DisplayName("−10 pumps own creatures +2/+2 and taps opponents' creatures")
    void minusTenPumpsAndTapsOpponents() {
        Permanent gideon = addReadyGideon(player1, 10);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opp = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(opp.isTapped()).isTrue();
        assertThat(bear.isTapped()).isFalse();
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);
    }

    private Permanent addReadyGideon(Player player, int loyalty) {
        Permanent perm = new Permanent(new GideonMartialParagon());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
