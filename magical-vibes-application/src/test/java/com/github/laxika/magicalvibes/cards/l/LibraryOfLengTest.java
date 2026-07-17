package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BurningInquiry;
import com.github.laxika.magicalvibes.cards.c.Catalog;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LibraryOfLengTest extends BaseCardTest {

    @Test
    @DisplayName("A chosen discard by the controller goes on top of their library instead of the graveyard")
    void chosenDiscardGoesOnTopOfLibrary() {
        harness.addToBattlefield(player1, new LibraryOfLeng());
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new Catalog(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Catalog drew two Islands; hand is GrizzlyBears + 2 Islands. Discard the GrizzlyBears.
        harness.handleCardChosen(player1, 0);

        // The discarded Grizzly Bears is put on top of the library, not into the graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Catalog"));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Random discards by the controller go on top of library; opponent without Leng still discards to graveyard")
    void randomDiscardRedirectOnlyForController() {
        harness.addToBattlefield(player1, new LibraryOfLeng());
        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new BurningInquiry()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Player 1 drew 3 and discarded 3 at random; with Library of Leng they go back on top of
        // the library, so the deck size is unchanged and only Burning Inquiry hits the graveyard.
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).singleElement()
                .matches(c -> c.getName().equals("Burning Inquiry"));

        // Player 2 has no Library of Leng — their 3 random discards go to the graveyard as normal.
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }
}
