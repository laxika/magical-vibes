package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
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

class GateToTheAetherTest extends BaseCardTest {

    @Test
    @DisplayName("Each player reveals their own top card and may put a permanent onto the battlefield")
    void eachPlayerUsesTheirOwnLibrary() {
        harness.addToBattlefield(player1, new GateToTheAether());
        harness.setLibrary(player1, deckOf(new DarksteelIngot(), new Blaze()));
        harness.setLibrary(player2, deckOf(new GrizzlyBears(), new Blaze()));

        runUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertOnBattlefield(player1, "Darksteel Ingot");

        runUpkeep(player2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Offers artifact, creature, enchantment, and land cards")
    void offersAllPrintedPermanentTypes() {
        harness.addToBattlefield(player1, new GateToTheAether());
        List<Card> cards = List.of(new DarksteelIngot(), new GrizzlyBears(), new GloriousAnthem(), new Forest());

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            harness.setLibrary(player1, deckOf(card, new Blaze()));
            runUpkeep(player1);
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            harness.assertOnBattlefield(player1, card.getName());
        }
    }

    @Test
    @DisplayName("Leaves a nonpermanent card on top without offering a choice")
    void nonpermanentCardStaysOnTop() {
        harness.addToBattlefield(player1, new GateToTheAether());
        harness.setLibrary(player1, deckOf(new Blaze(), new Forest()));

        runUpkeep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Blaze");
    }

    @Test
    @DisplayName("Leaves a matching card on top when declined")
    void declinedLeavesMatchingCardOnTop() {
        harness.addToBattlefield(player1, new GateToTheAether());
        harness.setLibrary(player1, deckOf(new Forest(), new Blaze()));

        runUpkeep(player1);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Forest");
    }

    private void runUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Card> deckOf(Card... cards) {
        return new ArrayList<>(List.of(cards));
    }
}
