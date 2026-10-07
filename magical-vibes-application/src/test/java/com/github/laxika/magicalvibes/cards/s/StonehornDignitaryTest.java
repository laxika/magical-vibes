package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FinestHour;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StonehornDignitary.class, RuneclawBear.class, Unsummon.class, FinestHour.class})
class StonehornDignitaryTest extends BaseCardTest {

    @Test
    @DisplayName("Entering flags the opponent to skip their next combat phase")
    void entersFlagsOpponent() {
        harness.setHand(player1, List.of(new StonehornDignitary()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("The controller is not a legal target for the enter trigger")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new StonehornDignitary()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Stonehorn Dignitary");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player1.getId(), 0)).isEqualTo(0);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("The flagged opponent jumps from precombat main straight to postcombat main")
    void flaggedOpponentSkipsCombat() {
        addCreatureReady(player2, new RuneclawBear());

        gd.skipNextCombatPhaseCount.put(player2.getId(), 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("A creature entering without being cast still triggers and waits for resolution")
    void enteringWithoutCastingTriggers() {
        harness.enterBattlefieldAndReturn(player1, new StonehornDignitary());
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isZero();

        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, harness::passBothPriorities);

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Two enter triggers skip two successive combats, then combat resumes")
    void multipleTriggersSkipSuccessiveCombats() {
        for (int i = 0; i < 2; i++) {
            harness.enterBattlefieldAndReturn(player1, new StonehornDignitary());
            harness.handlePermanentChosen(player1, player2.getId());
            harness.passBothPriorities();
        }

        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        harness.forceActivePlayer(player2);

        for (int remaining = 1; remaining >= 0; remaining--) {
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, harness::passBothPriorities);
            assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
            assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(remaining);
        }

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, harness::passBothPriorities);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @DisplayName("The controller's combat does not consume the opponent's skip")
    void controllersCombatIsUnaffected() {
        harness.enterBattlefieldAndReturn(player1, new StonehornDignitary());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, harness::passBothPriorities);

        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("An enter trigger during combat skips the next combat, not the current one")
    void triggerDuringCombatWaitsForNextCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.enterBattlefieldAndReturn(player1, new StonehornDignitary());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.POSTCOMBAT_MAIN);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, harness::passBothPriorities);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Removing the Dignitary in response does not stop its enter trigger")
    void triggerResolvesAfterSourceLeaves() {
        Permanent dignitary = harness.enterBattlefieldAndReturn(player1, new StonehornDignitary());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, dignitary.getId());
        harness.assertNotOnBattlefield(player1, "Stonehorn Dignitary");
        harness.assertInHand(player1, "Stonehorn Dignitary");
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isZero();

        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, harness::passBothPriorities);

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Skipping one of two extra combat phases preserves the other extra combat")
    void skippingExtraCombatPreservesFollowingExtraCombat() {
        addCreatureReady(player2, new RuneclawBear());
        harness.addToBattlefield(player2, new FinestHour());
        harness.addToBattlefield(player2, new FinestHour());
        gd.combatPhasesThisTurn = 1;
        declareAttackers(player2, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(2);

        harness.enterBattlefieldAndReturn(player1, new StonehornDignitary());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(1);

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, harness::passBothPriorities);

        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.additionalCombatPhasesOnly).isZero();
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isZero();
    }
}
