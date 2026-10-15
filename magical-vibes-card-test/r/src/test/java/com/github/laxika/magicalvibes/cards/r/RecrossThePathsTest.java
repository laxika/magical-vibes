package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RecrossThePaths.class, Forest.class, GrizzlyBears.class, Island.class, Shock.class})
class RecrossThePathsTest extends BaseCardTest {

    private void castRecross() {
        harness.setHand(player1, List.of(new RecrossThePaths()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void keepClashCardsOnTop() {
        while (gd.interaction.activeInteraction() instanceof PendingInteraction.Scry scry) {
            var player = scry.playerId().equals(player1.getId()) ? player1 : player2;
            gs.handleInteractionAnswer(gd, player,
                    new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        }
    }

    @Test
    @DisplayName("Reveals until a land, puts it onto the battlefield, bottoms the rest, and wins the clash")
    void revealsLandThenWinsClash() {
        Card shock = new Shock();
        Card land = new Forest();
        Card clashCard = new GrizzlyBears(); // mana value 2
        Card bottom = new Island();

        // Reveal order: Shock (nonland) -> Forest (land, stop). Forest enters, Shock goes to bottom.
        // After the reveal, the top of the library is Grizzly Bears (MV 2) for the clash.
        harness.setLibrary(player1, List.of(shock, land, clashCard, bottom));
        harness.setLibrary(player2, List.of(new Forest())); // MV 0 -> player1 wins

        castRecross();
        keepClashCardsOnTop();

        // Only one nonland was revealed, so it bottoms directly without a reorder choice.
        assertThat(gd.interaction.activeInteraction()).isNull();

        // The land entered the battlefield under player1's control.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == land);

        // The revealed nonland is now on the bottom of the library.
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(shock);

        // Winning the clash returned Recross the Paths to its owner's hand.
        harness.assertInHand(player1, "Recross the Paths");
        harness.assertNotInGraveyard(player1, "Recross the Paths");
    }

    @Test
    @DisplayName("Losing the clash still puts the land onto the battlefield but sends the spell to the graveyard")
    void revealsLandThenLosesClash() {
        Card land = new Forest();
        Card clashCard = new Forest(); // MV 0 -> player1 loses to Grizzly Bears

        // Reveal order: Forest (land, stop). It enters; the next Forest is the clash card.
        harness.setLibrary(player1, List.of(land, clashCard));
        harness.setLibrary(player2, List.of(new GrizzlyBears())); // MV 2 -> player1 loses

        castRecross();
        keepClashCardsOnTop();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == land);

        harness.assertInGraveyard(player1, "Recross the Paths");
        harness.assertNotInHand(player1, "Recross the Paths");
    }

    @Test
    @DisplayName("With no land in the library, the whole library is revealed and reordered onto the bottom")
    void noLandRevealsEntireLibrary() {
        Card shock = new Shock();
        Card bears = new GrizzlyBears();

        // No land anywhere in the library: reveal exhausts it, nothing enters the battlefield.
        harness.setLibrary(player1, List.of(shock, bears));
        harness.setLibrary(player2, List.of(new Forest()));

        castRecross();

        // Two revealed cards must be ordered onto the bottom.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        assertThat(reorder).containsExactlyInAnyOrder(shock, bears);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(reorder.indexOf(bears), reorder.indexOf(shock))));
        keepClashCardsOnTop();

        // Nothing entered the battlefield, and both cards are back in the library.
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears, shock);

        // The reordered library is available for the clash: Bears beats Forest.
        harness.assertInHand(player1, "Recross the Paths");
        harness.assertNotInGraveyard(player1, "Recross the Paths");
    }

    @Test
    @DisplayName("Clash placement can bottom both revealed cards without changing the winner")
    void bottomingClashCardsDoesNotChangeWinner() {
        Card bears = new GrizzlyBears();
        Card remaining = new Island();
        Card opponentTop = new Forest();
        Card opponentRemaining = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), bears, remaining));
        harness.setLibrary(player2, List.of(opponentTop, opponentRemaining));

        castRecross();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining, bears);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentRemaining, opponentTop);
        harness.assertInHand(player1, "Recross the Paths");
    }

    @Test
    @DisplayName("Cards revealed before the last land return to the library before the clash")
    void lastLandClashesWithReorderedNonlands() {
        Card shock = new Shock();
        Card bears = new GrizzlyBears();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(shock, bears, land));
        harness.setLibrary(player2, List.of(new Forest()));

        castRecross();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));
        keepClashCardsOnTop();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getCard() == land && !p.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears, shock);
        harness.assertInHand(player1, "Recross the Paths");
    }

    @Test
    @DisplayName("A tied clash does not return the spell")
    void tiedClashDoesNotReturnSpell() {
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        castRecross();
        keepClashCardsOnTop();

        harness.assertInGraveyard(player1, "Recross the Paths");
        harness.assertNotInHand(player1, "Recross the Paths");
    }

    @Test
    @DisplayName("An empty library reveals nothing and does not win against a revealed card")
    void emptyLibraryDoesNotWinClash() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        castRecross();
        keepClashCardsOnTop();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Recross the Paths");
    }
}
