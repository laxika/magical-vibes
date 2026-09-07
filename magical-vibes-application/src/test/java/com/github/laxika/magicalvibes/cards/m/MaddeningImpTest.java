package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaddeningImpTest extends BaseCardTest {

    /** player1 controls a ready Imp; it's player2's turn, in a step that precedes combat. */
    private Permanent primeImp() {
        Permanent imp = addCreatureReady(player1, new MaddeningImp());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return imp;
    }

    private void runEndStep() {
        harness.forceStep(TurnStep.END_STEP);
        harness.inMutationScope(
                () -> GameTestEngineContext.get().getBean(StepTriggerService.class).handleEndStepTriggers(gd));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Forces the active player's creatures to attack this turn if able")
    void forcesActivePlayersCreaturesToAttack() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        primeImp();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bear.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("At end step, destroys non-Wall creatures that didn't attack; spares Walls, attackers, and newly-controlled creatures")
    void destroysNonAttackersAtEndStep() {
        Permanent lazy = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttackedThisTurn(true);
        Permanent wall = addCreatureReady(player2, new WallOfAir());
        Permanent fresh = new Permanent(new GrizzlyBears());
        fresh.setSummoningSick(true);
        gd.playerBattlefields.get(player2.getId()).add(fresh);

        primeImp();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        runEndStep();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(lazy)
                .contains(attacker, wall, fresh);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot be activated during your own turn")
    void cannotActivateOnYourOwnTurn() {
        addCreatureReady(player1, new MaddeningImp());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent's turn");
    }

    @Test
    @DisplayName("Cannot be activated once the combat phase has begun")
    void cannotActivateDuringCombat() {
        primeImp();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before combat");
    }
}
