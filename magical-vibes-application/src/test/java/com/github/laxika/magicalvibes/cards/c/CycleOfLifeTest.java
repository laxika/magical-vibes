package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CycleOfLifeTest extends BaseCardTest {

    /** Casts a Grizzly Bears from player1's hand and resolves it, so it counts as "cast this turn". */
    private Permanent castBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Activating returns Cycle of Life to hand and makes the creature cast this turn 0/1")
    void setsBasePowerToughnessAndBouncesItself() {
        Permanent bears = castBears();
        harness.addToBattlefield(player1, new CycleOfLife());

        int cycleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Cycle of Life"));
        harness.activateAbility(player1, cycleIndex, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cycle of Life"));
        assertThat(countPermanents(player1, "Cycle of Life")).isZero();
    }

    @Test
    @DisplayName("The 0/1 lasts past end of turn — it is not an until-end-of-turn effect")
    void survivesEndOfTurnCleanup() {
        Permanent bears = castBears();
        harness.addToBattlefield(player1, new CycleOfLife());

        int cycleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Cycle of Life"));
        harness.activateAbility(player1, cycleIndex, null, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("At the controller's next upkeep the 0/1 ends and a +1/+1 counter is added")
    void endsAtNextUpkeepAndAddsCounter() {
        Permanent bears = castBears();
        harness.addToBattlefield(player1, new CycleOfLife());

        int cycleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Cycle of Life"));
        harness.activateAbility(player1, cycleIndex, null, bears.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature that was not cast this turn is an illegal target")
    void rejectsCreatureNotCastThisTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CycleOfLife());

        int cycleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Cycle of Life"));
        UUID targetId = bears.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, cycleIndex, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature an opponent cast this turn is an illegal target")
    void rejectsCreatureCastByOpponent() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new CycleOfLife());
        int cycleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Cycle of Life"));
        UUID targetId = bears.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, cycleIndex, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
