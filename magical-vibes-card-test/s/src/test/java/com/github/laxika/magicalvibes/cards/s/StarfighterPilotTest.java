package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ExosuitSavior;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StarfighterPilot.class, ExosuitSavior.class})
class StarfighterPilotTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Starfighter Pilot triggers surveil 1")
    void attackingTriggersSurveil() {
        addCreatureReady(player1, new StarfighterPilot());
        Card topCard = new ExosuitSavior();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Declining surveil 1 leaves the top card on the library")
    void decliningSurveilLeavesCardOnTop() {
        addCreatureReady(player1, new StarfighterPilot());
        Card topCard = new ExosuitSavior();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Another creature becoming tapped does not trigger Starfighter Pilot")
    void anotherCreatureBecomingTappedDoesNotTrigger() {
        addCreatureReady(player1, new StarfighterPilot());
        addCreatureReady(player1, new ExosuitSavior());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Surveil 1 puts only the top card into the graveyard")
    void surveilDoesNotMoveTheSecondCard() {
        addCreatureReady(player1, new StarfighterPilot());
        Card topCard = new ExosuitSavior();
        Card secondCard = new StarfighterPilot();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
    }

    @Test
    @DisplayName("An opponent's Pilot surveils its controller's library only")
    void opponentPilotSurveilsItsControllersLibrary() {
        addCreatureReady(player1, new StarfighterPilot());
        addCreatureReady(player2, new StarfighterPilot());
        Card ownTopCard = new ExosuitSavior();
        Card opponentTopCard = new ExosuitSavior();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of(opponentTopCard));

        declareAttackers(player2, List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentTopCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTopCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Surveilling an empty library finishes without a choice")
    void surveillingEmptyLibraryDoesNotRequireAChoice() {
        addCreatureReady(player1, new StarfighterPilot());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
