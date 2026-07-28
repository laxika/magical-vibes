package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MountainTitanTest extends BaseCardTest {

    @Test
    @DisplayName("After activating, casting a black spell puts a +1/+1 counter on it")
    void blackSpellPutsCounter() {
        Permanent titan = activateTitan();

        harness.setHand(player1, List.of(new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the delayed trigger

        assertThat(titan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a nonblack spell after activating puts no counter")
    void nonblackSpellPutsNoCounter() {
        Permanent titan = activateTitan();

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);

        assertThat(titan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting a black spell without activating puts no counter")
    void blackSpellWithoutActivationPutsNoCounter() {
        Permanent titan = addCreatureReady(player1, new MountainTitan());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);

        assertThat(titan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each black spell cast that turn triggers the ability again")
    void everyBlackSpellTriggers() {
        Permanent titan = activateTitan();

        harness.setHand(player1, List.of(new ScatheZombies(), new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveStack();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveStack();

        assertThat(titan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 10) {
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("The granted trigger stops working after the turn ends")
    void triggerExpiresAtEndOfTurn() {
        Permanent titan = activateTitan();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);

        assertThat(titan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    /**
     * Puts a Mountain Titan onto player1's battlefield and resolves its {@code {1}{R}{R}} ability,
     * leaving player1 with priority in their precombat main phase.
     */
    private Permanent activateTitan() {
        Permanent titan = addCreatureReady(player1, new MountainTitan());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(titan);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        return titan;
    }
}
