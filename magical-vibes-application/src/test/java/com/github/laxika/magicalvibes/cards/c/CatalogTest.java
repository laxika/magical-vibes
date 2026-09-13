package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Catalog.class, GrizzlyBears.class, Island.class})
class CatalogTest extends BaseCardTest {

    @Test
    @DisplayName("Casting draws two cards then discards one card")
    void drawsTwoThenDiscardsOne() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new Catalog(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // After drawing two, the effect awaits one discard choice.
        // Hand: 1 (GrizzlyBears left after cast) + 2 drawn = 3.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);

        harness.handleCardChosen(player1, 0);

        // Net: 1 (after cast), +2 draw, -1 discard = 2 cards.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        // The discarded card plus the resolved Catalog spell itself.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Catalog");
    }

    @Test
    @DisplayName("Can discard one of the cards it drew")
    void canDiscardDrawnCard() {
        Catalog catalog = new Catalog();
        Island firstDrawn = new Island();
        Island secondDrawn = new Island();

        harness.setLibrary(player1, List.of(firstDrawn, secondDrawn));
        harness.castFromHand(player1, catalog, "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDrawn, secondDrawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondDrawn);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(catalog, firstDrawn);
    }
}
