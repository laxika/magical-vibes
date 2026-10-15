package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GurmagAngler.class})
class GurmagAnglerTest extends BaseCardTest {

    @Test
    @DisplayName("Delve exiles graveyard cards to pay the generic creature cost")
    void delvePaysGenericCost() {
        List<Card> graveyard = List.of(
                new GurmagAngler(), new GurmagAngler(), new GurmagAngler(), new GurmagAngler(), new GurmagAngler());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new GurmagAngler()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3, 4));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);
        harness.assertOnBattlefield(player1, "Gurmag Angler");
    }

    @Test
    void fullDelveIsPaidBeforeResolution() {
        List<Card> graveyard = List.of(new GurmagAngler(), new GurmagAngler(), new GurmagAngler(),
                new GurmagAngler(), new GurmagAngler(), new GurmagAngler());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new GurmagAngler()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3, 4, 5));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Gurmag Angler");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Gurmag Angler");
    }

    @Test
    void canPayEntireCostWithManaWithoutDelving() {
        List<Card> graveyard = List.of(new GurmagAngler());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new GurmagAngler()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertOnBattlefield(player1, "Gurmag Angler");
    }

    @Test
    void partialDelveExilesOnlySelectedCardsFromCastersGraveyard() {
        List<Card> graveyard = List.of(new GurmagAngler(), new GurmagAngler(), new GurmagAngler());
        Card opponentsCard = new GurmagAngler();
        harness.setGraveyard(player1, graveyard);
        harness.setGraveyard(player2, List.of(opponentsCard));
        harness.setHand(player1, List.of(new GurmagAngler()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(2, 0));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyard.get(1));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(graveyard.get(0), graveyard.get(2));
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertOnBattlefield(player1, "Gurmag Angler");
    }

    @Test
    void delveCannotPayBlackManaRequirement() {
        List<Card> graveyard = List.of(new GurmagAngler(), new GurmagAngler(), new GurmagAngler(),
                new GurmagAngler(), new GurmagAngler(), new GurmagAngler());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new GurmagAngler()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(
                player1, 0, List.of(0, 1, 2, 3, 4, 5))).isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Gurmag Angler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotExileMoreCardsThanGenericCost() {
        List<Card> graveyard = List.of(new GurmagAngler(), new GurmagAngler(), new GurmagAngler(),
                new GurmagAngler(), new GurmagAngler(), new GurmagAngler(), new GurmagAngler());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new GurmagAngler()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(
                player1, 0, List.of(0, 1, 2, 3, 4, 5, 6))).isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Gurmag Angler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCountSameGraveyardCardTwice() {
        List<Card> graveyard = List.of(new GurmagAngler(), new GurmagAngler(), new GurmagAngler());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new GurmagAngler()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(
                player1, 0, List.of(0, 0))).isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Gurmag Angler");
        assertThat(gd.stack).isEmpty();
    }
}
