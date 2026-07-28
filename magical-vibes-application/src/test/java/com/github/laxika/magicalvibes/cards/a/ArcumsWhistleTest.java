package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArcumsWhistleTest extends BaseCardTest {

    /** Puts an untapped Arcum's Whistle onto player1's battlefield and gives them the {3}. */
    private Permanent addReadyWhistle() {
        Permanent whistle = addCreatureReady(player1, new ArcumsWhistle());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        return whistle;
    }

    /** Sets up player2 as the active player in their pre-combat main, with a Grizzly Bears out. */
    private Permanent setUpTargetForPlayer2() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        return bears;
    }

    private void runEndStep() {
        harness.forceStep(TurnStep.END_STEP);
        harness.inMutationScope(
                () -> GameTestEngineContext.get().getBean(StepTriggerService.class).handleEndStepTriggers(gd));
    }

    @Test
    @DisplayName("Declining the payment forces the target to attack this turn if able")
    void declineForcesAttack() {
        addReadyWhistle();
        Permanent bears = setUpTargetForPlayer2();

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(bears.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Declining the payment destroys the target at end step if it didn't attack")
    void declineDestroysAtEndStepIfItDidNotAttack() {
        addReadyWhistle();
        Permanent bears = setUpTargetForPlayer2();

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        runEndStep();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the payment spares the target at end step if it attacked")
    void declineSparesTargetThatAttacked() {
        addReadyWhistle();
        Permanent bears = setUpTargetForPlayer2();

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        bears.setAttackedThisTurn(true);

        runEndStep();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
    }

    @Test
    @DisplayName("Paying the creature's mana value avoids both halves of the penalty")
    void payingAvoidsPenalty() {
        addReadyWhistle();
        Permanent bears = setUpTargetForPlayer2();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(bears.isMustAttackThisTurn()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();

        runEndStep();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
    }

    @Test
    @DisplayName("Accepting without enough mana still applies the penalty")
    void acceptingWithoutManaAppliesPenalty() {
        addReadyWhistle();
        Permanent bears = setUpTargetForPlayer2();
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(bears.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a Wall")
    void rejectsWall() {
        addReadyWhistle();
        Permanent wall = addCreatureReady(player2, new WallOfAir());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature the active player doesn't control")
    void rejectsNonActiveController() {
        addReadyWhistle();
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, own.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate after attackers are declared")
    void cannotActivateAfterAttackersDeclared() {
        addReadyWhistle();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }
}
