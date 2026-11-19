package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.OnlyMatchingCreaturesCanAttackThisCombatEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BearWithSetsMechanic.class, GrizzlyBears.class})
class BearWithSetsMechanicTest extends BaseCardTest {

    @Test
    @DisplayName("Aggressive creates a restricted combat after the first combat")
    void createsRestrictedAggressiveCombat() {
        addCreatureReady(player1, new BearWithSetsMechanic());
        addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        gd.combatPhasesThisTurn = 1;
        harness.clearPriorityPassed();

        gs.advanceStep(gd);

        assertThat(gd.additionalCombatPhasesOnly).isZero();
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player1.getId()))
                .contains(0)
                .doesNotContain(1);
    }

    @Test
    @DisplayName("Aggressive does not create a combat after it is absent at first combat end")
    void doesNotCreateCombatWithoutAggressiveCreature() {
        addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        gd.combatPhasesThisTurn = 1;
        harness.clearPriorityPassed();

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }

    @Test
    @DisplayName("Several aggressive creatures make one aggressive combat, whose restriction ends with it")
    void multipleAggressiveCreaturesCreateOnlyOneAdditionalCombat() {
        addCreatureReady(player1, new BearWithSetsMechanic());
        addCreatureReady(player1, new BearWithSetsMechanic());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        gd.combatPhasesThisTurn = 1;
        harness.clearPriorityPassed();

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
        assertThat(gd.additionalCombatPhasesOnly).isZero();
        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player1.getId()))
                .containsExactly(0, 1);

        harness.forceStep(TurnStep.END_OF_COMBAT);
        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.floatingEffectsOfTypes(OnlyMatchingCreaturesCanAttackThisCombatEffect.class)).isEmpty();
    }

    @Test
    @DisplayName("Only the aggressive combat is restricted: a later added combat this turn lets every creature attack")
    void restrictionEndsWithTheAggressiveCombat() {
        addCreatureReady(player1, new BearWithSetsMechanic());
        addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        gd.combatPhasesThisTurn = 1;
        harness.clearPriorityPassed();

        gs.advanceStep(gd);
        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player1.getId()))
                .containsExactly(0);

        harness.forceStep(TurnStep.END_OF_COMBAT);
        gd.additionalCombatPhasesAfterMain = 1;
        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player1.getId()))
                .containsExactly(0, 1);
    }

    @Test
    void opponentsAggressiveCreatureDoesNotCreateAdditionalCombat() {
        addCreatureReady(player2, new BearWithSetsMechanic());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        gd.combatPhasesThisTurn = 1;
        harness.clearPriorityPassed();

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    void aggressiveCreatureEnteringAfterFirstCombatDoesNotCreateAdditionalCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        gd.combatPhasesThisTurn = 1;
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);

        addCreatureReady(player1, new BearWithSetsMechanic());
        gd.additionalCombatPhasesAfterMain = 1;
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);

        harness.forceStep(TurnStep.END_OF_COMBAT);
        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    void summoningSickAggressiveCreatureCreatesCombatButCannotAttack() {
        addCreatureReady(player1, new BearWithSetsMechanic()).setSummoningSick(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        gd.combatPhasesThisTurn = 1;
        harness.clearPriorityPassed();

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player1.getId()))
                .isEmpty();
    }

    @Test
    void tappedAggressiveCreatureCreatesCombatWithoutBeingUntapped() {
        var bear = addCreatureReady(player1, new BearWithSetsMechanic());
        bear.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        gd.combatPhasesThisTurn = 1;
        harness.clearPriorityPassed();

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(bear.isTapped()).isTrue();
        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player1.getId()))
                .isEmpty();
    }

    @Test
    void vigilanceAllowsBearToAttackInBothCombats() {
        var bear = addCreatureReady(player1, new BearWithSetsMechanic());
        gd.combatPhasesThisTurn = 1;

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(bear.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_OF_COMBAT);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(bear.isAttacking()).isTrue();
        assertThat(bear.isTapped()).isFalse();
    }
}
