package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EtherwroughtPageTest extends BaseCardTest {

    private static final String GAIN_LIFE = "You gain 2 life.";
    private static final String SURVEIL = "Surveil 1.";
    private static final String DRAIN = "Each opponent loses 1 life.";

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP, trigger fires
    }

    // ===== Mode: gain 2 life =====

    @Test
    @DisplayName("Choosing gain-life mode gains the controller 2 life")
    void gainLifeMode() {
        harness.addToBattlefield(player1, new EtherwroughtPage());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger — opens mode choice
        harness.handleListChoice(player1, GAIN_LIFE);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    // ===== Mode: each opponent loses 1 life =====

    @Test
    @DisplayName("Choosing drain mode makes each opponent lose 1 life")
    void drainMode() {
        harness.addToBattlefield(player1, new EtherwroughtPage());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger — opens mode choice
        harness.handleListChoice(player1, DRAIN);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
    }

    // ===== Mode: surveil 1 (accept — top card into graveyard) =====

    @Test
    @DisplayName("Choosing surveil mode and accepting puts the top card into the graveyard")
    void surveilModeAccepted() {
        harness.addToBattlefield(player1, new EtherwroughtPage());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger — opens mode choice
        harness.handleListChoice(player1, SURVEIL); // choose surveil — queues the may prompt
        harness.handleMayAbilityChosen(player1, true); // accept: put into graveyard

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Mode: surveil 1 (decline — leave on top) =====

    @Test
    @DisplayName("Choosing surveil mode and declining leaves the top card on the library")
    void surveilModeDeclined() {
        harness.addToBattlefield(player1, new EtherwroughtPage());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger — opens mode choice
        harness.handleListChoice(player1, SURVEIL);
        harness.handleMayAbilityChosen(player1, false); // decline: leave on top

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    // ===== Trigger only on controller's own upkeep =====

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new EtherwroughtPage());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    // ===== Illegal mode label is rejected =====

    @Test
    @DisplayName("An unknown mode label is rejected")
    void illegalModeRejected() {
        harness.addToBattlefield(player1, new EtherwroughtPage());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger — opens mode choice

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Draw seven cards."))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
