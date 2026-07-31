package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IntoTheWildsTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the top land onto the battlefield when accepted")
    void landOntoBattlefield() {
        addIntoTheWilds(player1);
        harness.setLibrary(player1, deckOf(new Forest(), new GrizzlyBears()));

        runUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Forest");
        // Grizzly Bears was the new top card and is drawn during the following draw step.
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Leaves the land on top of the library when declined")
    void declinedLeavesLandOnTop() {
        addIntoTheWilds(player1);
        harness.setLibrary(player1, deckOf(new Forest(), new GrizzlyBears()));

        runUpkeep(player1);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Forest");
        // Left on top of the library, so it is what the draw step draws.
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Offers no choice and leaves a nonland top card on the library")
    void nonlandTopCardStaysOnTop() {
        addIntoTheWilds(player1);
        harness.setLibrary(player1, deckOf(new GrizzlyBears(), new Forest()));

        runUpkeep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger on an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        addIntoTheWilds(player1);
        harness.setLibrary(player1, deckOf(new Forest(), new GrizzlyBears()));

        runUpkeep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    private void addIntoTheWilds(Player player) {
        harness.addToBattlefield(player, new IntoTheWilds());
    }

    private void runUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve the triggered ability
    }

    private List<Card> deckOf(Card... cards) {
        return new ArrayList<>(List.of(cards));
    }
}
