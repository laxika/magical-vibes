package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OracleEnVecTest extends BaseCardTest {

    private Permanent addReadyOracle() {
        return addCreatureReady(player1, new OracleEnVec());
    }

    private void activateOracle() {
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    private void runEndStep() {
        harness.forceStep(TurnStep.END_STEP);
        harness.inMutationScope(
                () -> GameTestEngineContext.get().getBean(StepTriggerService.class).handleEndStepTriggers(gd));
    }

    private void beginDeclareAttackersFor(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    @Test
    @DisplayName("Activating asks the targeted opponent to choose; the chosen creatures are registered for their next turn")
    void chosenCreaturesRegisteredForNextTurn() {
        addReadyOracle();
        Permanent chosen = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        activateOracle();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));

        assertThat(gd.chosenAttackersNextTurn).containsEntry(player2.getId(), Set.of(chosen.getId()));
    }

    @Test
    @DisplayName("With no creatures to choose, the empty selection is registered without asking")
    void noCreaturesRegistersEmptySelection() {
        addReadyOracle();

        activateOracle();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.chosenAttackersNextTurn).containsEntry(player2.getId(), Set.of());
    }

    @Test
    @DisplayName("The restriction activates when the chooser's turn begins and the pending entry is consumed")
    void activatesOnChoosersTurn() {
        Permanent chosen = addCreatureReady(player2, new GrizzlyBears());
        gd.chosenAttackersNextTurn.put(player2.getId(), Set.of(chosen.getId()));

        advanceTurn();

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.chosenAttackersThisTurn).containsEntry(player2.getId(), Set.of(chosen.getId()));
        assertThat(gd.chosenAttackersNextTurn).isEmpty();
    }

    @Test
    @DisplayName("A chosen creature attacks if able while the others can't attack at all")
    void chosenMustAttackOthersCant() {
        Permanent chosen = addCreatureReady(player2, new GrizzlyBears());
        Permanent unchosen = addCreatureReady(player2, new GrizzlyBears());
        gd.chosenAttackersThisTurn.put(player2.getId(), Set.of(chosen.getId()));

        beginDeclareAttackersFor(player2);

        List<Integer> attackable = harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player2.getId());
        assertThat(attackable).containsExactly(0);
        assertThat(harness.getCombatAttackService()
                .getMustAttackIndices(gd, player2.getId(), attackable)).contains(0);
        assertThat(harness.getAttackLegalityService().canAttack(gd, unchosen, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("Choosing no creatures locks every creature out of attacking that turn")
    void emptySelectionLocksAllAttackers() {
        addCreatureReady(player2, new GrizzlyBears());
        gd.chosenAttackersThisTurn.put(player2.getId(), Set.of());

        beginDeclareAttackersFor(player2);

        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A chosen creature that didn't attack is destroyed at that turn's end step")
    void chosenCreatureThatDidNotAttackIsDestroyed() {
        Permanent chosen = addCreatureReady(player2, new GrizzlyBears());
        gd.chosenAttackersNextTurn.put(player2.getId(), Set.of(chosen.getId()));

        advanceTurn();
        runEndStep();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(chosen);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(chosen.getCard());
    }

    @Test
    @DisplayName("A chosen creature that attacked survives the end step")
    void chosenCreatureThatAttackedSurvives() {
        Permanent chosen = addCreatureReady(player2, new GrizzlyBears());
        gd.chosenAttackersNextTurn.put(player2.getId(), Set.of(chosen.getId()));

        advanceTurn();
        chosen.setAttackedThisTurn(true);
        runEndStep();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(chosen);
    }

    @Test
    @DisplayName("The ability can't target its own controller")
    void cannotTargetSelf() {
        addReadyOracle();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
