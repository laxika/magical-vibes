package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EtherwroughtPage.class, EsperStormblade.class})
class EtherwroughtPageTest extends BaseCardTest {

    private static final String GAIN_LIFE = "You gain 2 life.";
    private static final String SURVEIL = "Surveil 1.";
    private static final String DRAIN = "Each opponent loses 1 life.";

    @Test
    @DisplayName("Choosing gain-life mode gains the controller 2 life")
    void gainLifeMode() {
        harness.addToBattlefield(player1, new EtherwroughtPage());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, GAIN_LIFE);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Choosing drain mode makes each opponent lose 1 life")
    void drainMode() {
        harness.addToBattlefield(player1, new EtherwroughtPage());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, DRAIN);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
    }

    @Test
    @DisplayName("Choosing surveil mode and accepting puts the top card into the graveyard")
    void surveilModeAccepted() {
        harness.addToBattlefield(player1, new EtherwroughtPage());
        Card topCard = new EsperStormblade();
        harness.setLibrary(player1, List.of(topCard));
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, SURVEIL);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true); // accept: put into graveyard

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 1);
        harness.assertInGraveyard(player1, "Esper Stormblade");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing surveil mode and declining leaves the top card on the library")
    void surveilModeDeclined() {
        harness.addToBattlefield(player1, new EtherwroughtPage());
        Card topCard = new EsperStormblade();
        harness.setLibrary(player1, List.of(topCard));
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, SURVEIL);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false); // decline: leave on top

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new EtherwroughtPage());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An unknown mode label is rejected")
    void illegalModeRejected() {
        harness.addToBattlefield(player1, new EtherwroughtPage());

        advanceToUpkeep(player1);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Draw seven cards."))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("The mode is chosen before either player can respond to the upkeep trigger")
    void modeChosenBeforeResponses() {
        harness.addToBattlefield(player1, new EtherwroughtPage());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, GAIN_LIFE);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Surveil with an empty library resolves without a choice or life change")
    void surveilEmptyLibrary() {
        harness.addToBattlefield(player1, new EtherwroughtPage());
        harness.setLibrary(player1, List.of());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, SURVEIL);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }
}
