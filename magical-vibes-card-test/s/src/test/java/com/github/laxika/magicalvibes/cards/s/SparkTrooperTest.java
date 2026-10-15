package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArmoredTransport;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SparkTrooper.class, ArmoredTransport.class})
class SparkTrooperTest extends BaseCardTest {

    @Test
    @DisplayName("Attacks immediately with haste, dealing 6 and gaining 6 life from lifelink")
    void attacksImmediatelyAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent trooper = harness.addToBattlefieldAndReturn(player1, new SparkTrooper());
        trooper.setSummoningSick(true);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
    }

    @Test
    @DisplayName("Sacrifices itself at the beginning of the end step")
    void sacrificesItselfAtEndStep() {
        Permanent trooper = harness.addToBattlefieldAndReturn(player1, new SparkTrooper());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getSourcePermanentId()).isEqualTo(trooper.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spark Trooper");
        harness.assertInGraveyard(player1, "Spark Trooper");
    }

    @Test
    @DisplayName("Trample deals excess damage and lifelink counts damage to both blocker and player")
    void tramplesAndGainsLifeEvenWhenItDiesInCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent trooper = addCreatureReady(player1, new SparkTrooper());
        trooper.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ArmoredTransport());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1, player2.getId(), 5));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
        harness.assertInGraveyard(player1, "Spark Trooper");
        harness.assertInGraveyard(player2, "Armored Transport");
    }

    @Test
    @DisplayName("Sacrifices itself during the opponent's end step too")
    void sacrificesAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new SparkTrooper());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Spark Trooper");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spark Trooper");
        harness.assertInGraveyard(player1, "Spark Trooper");
    }

    @Test
    @DisplayName("A Trooper entering after the end step begins waits until the next end step")
    void enteringDuringEndStepWaitsForNextEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.enterBattlefieldAndReturn(player1, new SparkTrooper());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Spark Trooper");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spark Trooper");
        harness.assertInGraveyard(player1, "Spark Trooper");
    }
}
