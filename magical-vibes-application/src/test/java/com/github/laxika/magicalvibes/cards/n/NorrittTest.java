package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
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

class NorrittTest extends BaseCardTest {

    private Permanent addReadyNorritt() {
        return addCreatureReady(player1, new Norritt());
    }

    private void runEndStep() {
        harness.forceStep(TurnStep.END_STEP);
        GameTestEngineContext.get().getBean(StepTriggerService.class).handleEndStepTriggers(gd);
    }

    @Test
    @DisplayName("First ability untaps target blue creature")
    void untapsTargetBlueCreature() {
        Permanent norritt = addReadyNorritt();
        Permanent merfolk = addCreatureReady(player2, new MerfolkOfThePearlTrident());
        merfolk.tap();

        harness.activateAbility(player1, 0, 0, null, merfolk.getId());
        harness.passBothPriorities();

        assertThat(merfolk.isTapped()).isFalse();
        assertThat(norritt.isTapped()).isTrue();
    }

    @Test
    @DisplayName("First ability cannot target a nonblue creature")
    void cannotUntapNonblue() {
        addReadyNorritt();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability forces target to attack this turn if able")
    void forcesTargetToAttack() {
        addReadyNorritt();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("At end step, destroys the target if it didn't attack")
    void destroysIfDidNotAttack() {
        addReadyNorritt();
        Permanent lazy = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.activateAbility(player1, 0, 1, null, lazy.getId());
        harness.passBothPriorities();

        runEndStep();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(lazy);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("At end step, spares the target if it attacked")
    void sparesIfAttacked() {
        addReadyNorritt();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttackedThisTurn(true);

        runEndStep();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Cannot target a Wall or a summoning-sick creature")
    void rejectsWallAndSummoningSick() {
        addReadyNorritt();
        Permanent wall = addCreatureReady(player2, new WallOfAir());
        Permanent fresh = new Permanent(new GrizzlyBears());
        fresh.setSummoningSick(true);
        gd.playerBattlefields.get(player2.getId()).add(fresh);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, fresh.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature not controlled by the active player")
    void rejectsNonActiveController() {
        addReadyNorritt();
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, own.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate second ability after attackers are declared")
    void cannotActivateAfterAttackersDeclared() {
        addReadyNorritt();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate second ability during a second combat phase")
    void cannotActivateInSecondCombat() {
        addReadyNorritt();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        gd.combatPhasesThisTurn = 2;

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }
}
