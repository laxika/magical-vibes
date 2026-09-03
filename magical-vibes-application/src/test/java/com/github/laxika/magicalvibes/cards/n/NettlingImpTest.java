package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NettlingImp.class, GrizzlyBears.class, WallOfAir.class})
class NettlingImpTest extends BaseCardTest {

    private void setOpponentBeforeAttackers() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
    }

    private void runEndStep() {
        harness.forceStep(TurnStep.END_STEP);
        harness.inMutationScope(
                () -> GameTestEngineContext.get().getBean(StepTriggerService.class).handleEndStepTriggers(gd));
    }

    @Test
    @DisplayName("Forces the target to attack this turn if able")
    void forcesTargetToAttack() {
        addCreatureReady(player1, new NettlingImp());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        setOpponentBeforeAttackers();

        harness.activateAbility(player1, 0, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Requires the target to be declared as an attacker when it can attack")
    void requiresTargetToAttackWhenAble() {
        addCreatureReady(player1, new NettlingImp());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        setOpponentBeforeAttackers();

        harness.activateAbility(player1, 0, 0, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(target.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Destroys the target at the next end step if it did not attack")
    void destroysIfDidNotAttack() {
        addCreatureReady(player1, new NettlingImp());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        setOpponentBeforeAttackers();

        harness.activateAbility(player1, 0, 0, 0, target.getId());
        harness.passBothPriorities();
        runEndStep();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Allows a tapped target and destroys it because it cannot attack")
    void allowsTappedTargetAndDestroysIt() {
        addCreatureReady(player1, new NettlingImp());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        setOpponentBeforeAttackers();

        harness.activateAbility(player1, 0, 0, 0, target.getId());
        harness.passBothPriorities();
        runEndStep();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Still destroys the target if Nettling Imp leaves the battlefield")
    void delayedDestructionSurvivesSourceLeavingBattlefield() {
        Permanent imp = addCreatureReady(player1, new NettlingImp());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        setOpponentBeforeAttackers();

        harness.activateAbility(player1, 0, 0, 0, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(imp);

        runEndStep();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Spares the target at the next end step if it attacked")
    void sparesIfAttacked() {
        addCreatureReady(player1, new NettlingImp());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        setOpponentBeforeAttackers();

        harness.activateAbility(player1, 0, 0, 0, target.getId());
        harness.passBothPriorities();
        declareAttackers(player2, List.of(0));
        runEndStep();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Rejects Walls and creatures not controlled continuously since the turn began")
    void rejectsWallAndSummoningSickCreature() {
        addCreatureReady(player1, new NettlingImp());
        Permanent wall = addCreatureReady(player2, new WallOfAir());
        Permanent fresh = new Permanent(new GrizzlyBears());
        fresh.setSummoningSick(true);
        gd.playerBattlefields.get(player2.getId()).add(fresh);
        setOpponentBeforeAttackers();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, fresh.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a creature controlled by a nonactive player")
    void rejectsNonActiveController() {
        addCreatureReady(player1, new NettlingImp());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        setOpponentBeforeAttackers();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate only during an opponent's turn and before attackers are declared")
    void enforcesActivationTiming() {
        addCreatureReady(player1, new NettlingImp());
        Permanent ownTarget = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, ownTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent's turn");

        Permanent opponentTarget = addCreatureReady(player2, new GrizzlyBears());
        setOpponentBeforeAttackers();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, opponentTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }
}
