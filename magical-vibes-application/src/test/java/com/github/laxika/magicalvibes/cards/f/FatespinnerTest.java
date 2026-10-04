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
    @DisplayName("Choosing draw step skips the entire step and prevents the turn-based draw")
    void skipsDrawStep() {
        gd.turnNumber = 2; // avoid the starting player's first-turn draw skip
        beginChoice();

        int handSize = gd.playerHands.get(player2.getId()).size();
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.withAutoStop(TurnStep.DRAW,
                        () -> harness.handleListChoice(player2, DRAW_STEP)));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gameLogContains("Step: Draw")).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize);
        assertThat(gd.skippedStepOrPhasesThisTurn.get(player2.getId()))
                .containsExactly(SkipStepOrPhaseKind.DRAW_STEP);
    }

    @Test
    @DisplayName("Choosing main phase skips both of that turn's main phases")
    void skipsMainPhases() {
        beginChoice();
        harness.withAutoStop(TurnStep.END_STEP,
                () -> harness.handleListChoice(player2, MAIN_PHASE));
        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gameLogContains("Step: Precombat Main")).isFalse();
        assertThat(gameLogContains("Step: Postcombat Main")).isFalse();
        assertThat(gd.gameLog.stream()
                .filter(entry -> entry.plainText().contains("skips their main phase.")))
                .hasSize(2);
    }

    @Test
    @DisplayName("Choosing combat phase skips the combat phase")
    void skipsCombatPhase() {
        addCreatureReady(player2, new AlphaMyr());

        beginChoice();
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN,
                () -> harness.handleListChoice(player2, COMBAT_PHASE));
        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);

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

    @Test
    @DisplayName("The upkeep trigger still applies after Fatespinner leaves the battlefield")
    void triggerSurvivesSourceLeavingBattlefield() {
        beginChoice();
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN,
                () -> harness.handleListChoice(player2, COMBAT_PHASE));
        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gameLogContains("skips their combat phase.")).isTrue();
        assertThat(gameLogContains("Step: Beginning of Combat")).isFalse();
    }

    @Test
    @DisplayName("The chosen skip expires at the end of the affected turn")
    void chosenSkipExpiresAtTurnEnd() {
        beginChoice();
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN,
                () -> harness.handleListChoice(player2, COMBAT_PHASE));
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.skippedStepOrPhasesThisTurn).isEmpty();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
    }

    private void beginChoice() {
        harness.addToBattlefield(player1, new Fatespinner());
        gd.gameLog.clear();
        advanceToUpkeep(player2);
        harness.passBothPriorities();
    }
}
