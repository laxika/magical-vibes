package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecrossThePathsTest extends BaseCardTest {

    private void castRecross() {
        harness.setHand(player1, List.of(new RecrossThePaths()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
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
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(shock, land, clashCard, bottom));
        gd.playerDecks.get(player2.getId()).addFirst(new Forest()); // MV 0 -> player1 wins

        castRecross();

        // Only one nonland was revealed, so it bottoms directly without a reorder choice.
        assertThat(gd.interaction.activeInteraction()).isNull();

        // The land entered the battlefield under player1's control.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == land);

        // The revealed nonland is now on the bottom of the library.
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(shock);

        // Winning the clash returned Recross the Paths to its owner's hand.
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Recross the Paths"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Recross the Paths"));
    }

    @Test
    @DisplayName("Losing the clash still puts the land onto the battlefield but sends the spell to the graveyard")
    void revealsLandThenLosesClash() {
        Card land = new Forest();
        Card clashCard = new Forest(); // MV 0 -> player1 loses to Grizzly Bears

        // Reveal order: Forest (land, stop). It enters; the next Forest is the clash card.
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(land, clashCard));
        gd.playerDecks.get(player2.getId()).addFirst(new GrizzlyBears()); // MV 2 -> player1 loses

        castRecross();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == land);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Recross the Paths"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Recross the Paths"));
    }

    @Test
    @DisplayName("With no land in the library, the whole library is revealed and reordered onto the bottom")
    void noLandRevealsEntireLibrary() {
        Card shock = new Shock();
        Card bears = new GrizzlyBears();

        // No land anywhere in the library: reveal exhausts it, nothing enters the battlefield.
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(shock, bears));
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        castRecross();

        // Two revealed cards must be ordered onto the bottom.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        assertThat(reorder).containsExactlyInAnyOrder(shock, bears);
        harness.getGameService().handleLibraryCardsReordered(gd, player1,
                List.of(reorder.indexOf(bears), reorder.indexOf(shock)));

        // Nothing entered the battlefield, and both cards are back in the library.
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears, shock);

        // The clash was made with an empty library, so player1 loses and the spell is in the graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Recross the Paths"));
    }
}
