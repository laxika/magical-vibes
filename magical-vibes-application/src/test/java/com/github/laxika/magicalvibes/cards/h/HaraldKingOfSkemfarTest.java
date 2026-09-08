package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HaraldKingOfSkemfarTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers Elf, Warrior, and Tyvar cards among the top five")
    void etbOffersMatchingCards() {
        Card elf = card("Llanowar Elves", CardType.CREATURE, CardSubtype.ELF);
        Card warrior = card("Kor Blademaster", CardType.CREATURE, CardSubtype.WARRIOR);
        Card tyvar = card("Tyvar, Jubilant Brawler", CardType.PLANESWALKER, CardSubtype.TYVAR);
        Card bear = card("Grizzly Bears", CardType.CREATURE, CardSubtype.BEAR);
        Card shock = card("Shock", CardType.INSTANT);
        setTopCards(List.of(elf, warrior, tyvar, bear, shock));

        castAndResolve();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(elf, warrior, tyvar, bear, shock);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(elf.getId(), warrior.getId(), tyvar.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing a matching card puts it into hand and randomly bottoms the rest")
    void choosingMatchingCardPutsItIntoHand() {
        Card elf = card("Llanowar Elves", CardType.CREATURE, CardSubtype.ELF);
        Card warrior = card("Kor Blademaster", CardType.CREATURE, CardSubtype.WARRIOR);
        Card tyvar = card("Tyvar, Jubilant Brawler", CardType.PLANESWALKER, CardSubtype.TYVAR);
        Card bear = card("Grizzly Bears", CardType.CREATURE, CardSubtype.BEAR);
        Card shock = card("Shock", CardType.INSTANT);
        setTopCards(List.of(elf, warrior, tyvar, bear, shock));

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of(tyvar.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(tyvar);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(elf, warrior, bear, shock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cards beyond the top five are not eligible")
    void onlyTopFiveAreEligible() {
        Card bear = card("Grizzly Bears", CardType.CREATURE, CardSubtype.BEAR);
        Card shock = card("Shock", CardType.INSTANT);
        Card forest = card("Forest", CardType.LAND, CardSubtype.FOREST);
        Card plains = card("Plains", CardType.LAND, CardSubtype.PLAINS);
        Card swamp = card("Swamp", CardType.LAND, CardSubtype.SWAMP);
        Card elfBelowTopFive = card("Llanowar Elves", CardType.CREATURE, CardSubtype.ELF);
        setTopCards(List.of(bear, shock, forest, plains, swamp, elfBelowTopFive));

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).contains(elfBelowTopFive);
    }

    private static Card card(String name, CardType type, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    private void setTopCards(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new HaraldKingOfSkemfar()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
