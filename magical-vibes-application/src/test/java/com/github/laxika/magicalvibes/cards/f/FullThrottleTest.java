package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HowlersHeavy;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FullThrottle.class, HowlersHeavy.class})
class FullThrottleTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two combat phases after the main phase and untaps attacked creatures at each one")
    void createsTwoCombatPhasesAndUntapsAttackedCreaturesEachCombat() {
        Permanent bear = addCreatureReady(player1, new HowlersHeavy());
        declareAttackers(List.of(0));
        assertThat(bear.isTapped()).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new FullThrottle(), "{4}{R}{R}");
        harness.passBothPriorities();
        assertThat(gd.additionalCombatPhasesAfterMain).isEqualTo(2);

        harness.passUntil(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(bear.isTapped()).isFalse();

        bear.tap();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(TurnStep.DECLARE_ATTACKERS);

        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
        assertThat(gd.additionalCombatPhasesAfterMain).isZero();
        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting before combat creates two extra combats before the normal combat, without intervening main phases")
    void precombatCastPreservesNormalCombat() {
        Permanent attacker = addCreatureReady(player1, new HowlersHeavy());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new FullThrottle(), "{4}{R}{R}");
        harness.passBothPriorities();

        harness.passUntil(TurnStep.DECLARE_ATTACKERS);
        declareAttackers(List.of(0));
        assertThat(attacker.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.DECLARE_ATTACKERS);
        assertThat(attacker.isTapped()).isFalse();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);

        declareAttackers(List.of(0));
        assertThat(attacker.isTapped()).isTrue();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.DECLARE_ATTACKERS);
        assertThat(attacker.isTapped()).isFalse();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(3);

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("The delayed ability uses the stack and leaves creatures that did not attack tapped")
    void untapsOnlyCreaturesThatAttackedWhenTriggerResolves() {
        Permanent attacker = addCreatureReady(player1, new HowlersHeavy());
        Permanent nonattacker = addCreatureReady(player1, new HowlersHeavy());
        Permanent opponentCreature = addCreatureReady(player2, new HowlersHeavy());
        opponentCreature.tap();
        declareAttackers(List.of(0));
        nonattacker.tap();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new FullThrottle(), "{4}{R}{R}");
        harness.passBothPriorities();

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);
        assertThat(attacker.isTapped()).isTrue();
        resolveAllTriggers();

        assertThat(attacker.isTapped()).isFalse();
        assertThat(nonattacker.isTapped()).isTrue();
        assertThat(opponentCreature.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.DECLARE_ATTACKERS);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }
}
