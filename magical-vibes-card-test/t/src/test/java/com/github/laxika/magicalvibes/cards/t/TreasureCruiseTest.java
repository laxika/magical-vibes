package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.f.ForceAway;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TreasureCruise.class, AlpineGrizzly.class, ForceAway.class})
class TreasureCruiseTest extends BaseCardTest {

    @Test
    @DisplayName("Delve pays the generic cost and draws three cards")
    void delvesAndDrawsThreeCards() {
        Card first = new AlpineGrizzly();
        Card second = new ForceAway();
        Card third = new AlpineGrizzly();
        harness.setLibrary(player1, List.of(first, second, third));
        List<Card> graveyard = List.of(
                new ForceAway(), new AlpineGrizzly(), new ForceAway(), new AlpineGrizzly(), new ForceAway());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new TreasureCruise()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, null, List.of(0, 1, 2, 3, 4));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        harness.assertInGraveyard(player1, "Treasure Cruise");
    }

    @Test
    @DisplayName("Delve is optional even with cards in the graveyard")
    void paysFullManaWithoutDelving() {
        List<Card> library = List.of(new AlpineGrizzly(), new AlpineGrizzly(), new AlpineGrizzly());
        Card graveyardCard = new AlpineGrizzly();
        harness.setLibrary(player1, library);
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new TreasureCruise()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Treasure Cruise");
    }

    @Test
    @DisplayName("Seven delved cards leave only the blue mana requirement")
    void delvesSevenCardsAndLeavesUnselectedCards() {
        List<Card> library = List.of(new AlpineGrizzly(), new AlpineGrizzly(), new AlpineGrizzly());
        List<Card> graveyard = List.of(new AlpineGrizzly(), new AlpineGrizzly(), new AlpineGrizzly(),
                new AlpineGrizzly(), new AlpineGrizzly(), new AlpineGrizzly(), new AlpineGrizzly(),
                new AlpineGrizzly());
        harness.setLibrary(player1, library);
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new TreasureCruise()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, null, List.of(0, 1, 2, 3, 4, 5, 6));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyard.get(7));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(graveyard.subList(0, 7));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(library);
        harness.assertInGraveyard(player1, "Treasure Cruise");
    }

    @Test
    @DisplayName("Delve cannot pay the blue mana requirement")
    void cannotReplaceBlueManaWithDelve() {
        List<Card> graveyard = List.of(new AlpineGrizzly(), new AlpineGrizzly(), new AlpineGrizzly(),
                new AlpineGrizzly(), new AlpineGrizzly(), new AlpineGrizzly(), new AlpineGrizzly());
        Card cruise = new TreasureCruise();
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(cruise));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithMultipleGraveyardExile(
                player1, 0, null, List.of(0, 1, 2, 3, 4, 5, 6)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cruise);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Delve cannot exile more cards than the generic mana requirement")
    void cannotDelveEightCards() {
        List<Card> graveyard = List.of(new AlpineGrizzly(), new AlpineGrizzly(), new AlpineGrizzly(),
                new AlpineGrizzly(), new AlpineGrizzly(), new AlpineGrizzly(), new AlpineGrizzly(),
                new AlpineGrizzly());
        Card cruise = new TreasureCruise();
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(cruise));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstantWithMultipleGraveyardExile(
                player1, 0, null, List.of(0, 1, 2, 3, 4, 5, 6, 7)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cruise);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("The same graveyard card cannot pay for two generic mana")
    void cannotDelveTheSameCardTwice() {
        Card graveyardCard = new AlpineGrizzly();
        Card cruise = new TreasureCruise();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(cruise));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castInstantWithMultipleGraveyardExile(
                player1, 0, null, List.of(0, 0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cruise);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(6);
    }
}
