package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UnexpectedResultsTest extends BaseCardTest {

    /**
     * The card shuffles before revealing, so the library is stacked with a single card to make
     * "the top card" deterministic.
     */
    private void stackLibrary(Card only) {
        List<Card> deck = new ArrayList<>();
        if (only != null) {
            deck.add(only);
        }
        gd.playerDecks.put(player1.getId(), deck);
    }

    private void castUnexpectedResults() {
        harness.setHand(player1, List.of(new UnexpectedResults()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Nonland top card may be cast without paying its mana cost")
    void nonlandTopCardIsCastForFree() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        stackLibrary(new Pyroclasm());

        castUnexpectedResults();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Unexpected Results");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the nonland card leaves it on top of the library")
    void declinedNonlandStaysOnTop() {
        Card pyroclasm = new Pyroclasm();
        stackLibrary(pyroclasm);

        castUnexpectedResults();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(pyroclasm);
        harness.assertInGraveyard(player1, "Unexpected Results");
    }

    @Test
    @DisplayName("Land top card is put onto the battlefield and Unexpected Results returns to hand")
    void landIsPutOntoBattlefieldAndSpellReturnsToHand() {
        stackLibrary(new Forest());

        castUnexpectedResults();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Unexpected Results");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Putting the land onto the battlefield does not use the land drop for the turn")
    void landDoesNotCountAsLandPlay() {
        stackLibrary(new Forest());

        castUnexpectedResults();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.landsPlayedThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Declining the land leaves it on top and Unexpected Results goes to the graveyard")
    void declinedLandStaysOnTop() {
        Card forest = new Forest();
        stackLibrary(forest);

        castUnexpectedResults();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertInGraveyard(player1, "Unexpected Results");
    }

    @Test
    @DisplayName("An empty library offers no choice")
    void emptyLibraryDoesNothing() {
        stackLibrary(null);

        castUnexpectedResults();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Unexpected Results");
    }
}
