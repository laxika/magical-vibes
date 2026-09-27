package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.SkipStepOrPhaseKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fatespinner.class, AlphaMyr.class})
class FatespinnerTest extends BaseCardTest {

    private static final String DRAW_STEP = "Draw step";
    private static final String MAIN_PHASE = "Main phase";
    private static final String COMBAT_PHASE = "Combat phase";

    @Test
    @DisplayName("The opponent chooses the step or phase to skip")
    void opponentChoosesMode() {
        beginChoice();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).containsExactly(DRAW_STEP, MAIN_PHASE, COMBAT_PHASE);
    }

    @Test
    @DisplayName("Choosing draw step prevents the turn-based draw")
    void skipsDrawStep() {
        gd.turnNumber = 2; // avoid the starting player's first-turn draw skip
        beginChoice();

        int handSize = gd.playerHands.get(player2.getId()).size();
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        harness.withAutoStop(TurnStep.DRAW,
                () -> harness.handleListChoice(player2, DRAW_STEP));

        assertThat(gd.currentStep).isEqualTo(TurnStep.DRAW);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize);
        assertThat(gd.skippedStepOrPhasesThisTurn.get(player2.getId()))
                .containsExactly(SkipStepOrPhaseKind.DRAW_STEP);
    }

    @Test
    @DisplayName("Choosing main phase skips both of that turn's main phases")
    void skipsMainPhases() {
        beginChoice();
        harness.handleListChoice(player2, MAIN_PHASE);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);

        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.gameLog.stream()
                .filter(entry -> entry.plainText().contains("skips their main phase.")))
                .hasSize(2);
    }

    @Test
    @DisplayName("Choosing combat phase skips the combat phase")
    void skipsCombatPhase() {
        addCreatureReady(player2, new AlphaMyr());

        beginChoice();
        harness.handleListChoice(player2, COMBAT_PHASE);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gameLogContains("skips their combat phase.")).isTrue();
        assertThat(gd.skippedStepOrPhasesThisTurn.get(player2.getId()))
                .containsExactly(SkipStepOrPhaseKind.COMBAT_PHASE);
    }

    @Test
    @DisplayName("Does not trigger during its controller's upkeep")
    void doesNotTriggerDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new Fatespinner());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void beginChoice() {
        harness.addToBattlefield(player1, new Fatespinner());
        advanceToUpkeep(player2);
        harness.passBothPriorities();
    }
}
