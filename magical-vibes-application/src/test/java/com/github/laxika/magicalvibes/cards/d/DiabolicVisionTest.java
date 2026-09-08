package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.FyndhornElves;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiabolicVision.class, BalduvianBears.class, FyndhornElves.class, Plains.class,
        Island.class, Mountain.class})
class DiabolicVisionTest extends BaseCardTest {

    private void castVision() {
        harness.setHand(player1, List.of(new DiabolicVision()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Chosen card goes to hand and the rest go back on top in the chosen order")
    void chosenCardToHandRestOnTop() {
        Card c0 = new BalduvianBears();
        Card c1 = new FyndhornElves();
        Card c2 = new Plains();
        Card c3 = new Island();
        Card c4 = new Mountain();
        Card belowTopFive = new BalduvianBears();
        harness.setLibrary(player1, List.of(c0, c1, c2, c3, c4, belowTopFive));

        castVision();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(c0, c1, c2, c3, c4);

        // Put Plains (index 2) into hand.
        harness.handleCardChosen(player1, 2);
        assertThat(gd.playerHands.get(player1.getId())).contains(c2);

        // The remaining four are reordered back on top.
        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(c0, c1, c3, c4);
        assertThat(reorder.toBottom()).isFalse();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 1, 0, 2)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(c4, c1, c0, c3, belowTopFive);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With fewer than five cards, the leftovers still go back on top")
    void worksWithSmallLibrary() {
        Card c0 = new BalduvianBears();
        Card c1 = new FyndhornElves();
        harness.setLibrary(player1, List.of(c0, c1));

        castVision();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(c0);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(c1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A one-card library simply puts that card into hand")
    void singleCardLibraryGoesToHand() {
        Card only = new Island();
        harness.setLibrary(player1, List.of(only));

        castVision();

        assertThat(gd.playerHands.get(player1.getId())).contains(only);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With an empty library, Diabolic Vision does nothing")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());

        castVision();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
