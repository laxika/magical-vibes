package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClifftopLookout.class, Forest.class})
class ClifftopLookoutTest extends BaseCardTest {

    @Test
    @DisplayName("Enters a land tapped and puts preceding revealed cards on the library bottom")
    void entersLandTappedAndBottomsRevealedCards() {
        Card nonland = new ClifftopLookout();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(nonland, forest));
        castClifftopLookout();

        Permanent enteredLand = findPermanent(player1, forest.getName());
        assertThat(enteredLand.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
    }

    @Test
    @DisplayName("Returns all revealed cards without asking for an order when no land is found")
    void randomlyBottomsAllRevealedCardsWhenNoLandIsFound() {
        Card first = new ClifftopLookout();
        Card second = new ClifftopLookout();
        harness.setLibrary(player1, List.of(first, second));
        castClifftopLookout();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == first || permanent.getCard() == second);
    }

    @Test
    @DisplayName("Bottoms multiple revealed cards automatically and leaves unrevealed cards on top")
    void bottomsMultipleCardsWithoutAnOrderingChoice() {
        Card first = new ClifftopLookout();
        Card second = new ClifftopLookout();
        Card land = new Forest();
        Card unrevealed = new Forest();
        harness.setLibrary(player1, List.of(first, second, land, unrevealed));
        castClifftopLookout();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unrevealed);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(first, second);
        assertThat(findPermanent(player1, land.getName()).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Stops at a land on top without revealing or moving later cards")
    void stopsAtTopLand() {
        Card land = new Forest();
        Card next = new ClifftopLookout();
        Card laterLand = new Forest();
        harness.setLibrary(player1, List.of(land, next, laterLand));
        castClifftopLookout();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, laterLand);
        assertThat(findPermanent(player1, land.getName()).isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Resolves with an empty library without requesting input")
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        castClifftopLookout();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void castClifftopLookout() {
        harness.setHand(player1, List.of(new ClifftopLookout()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
