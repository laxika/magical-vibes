package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class SylvanMessengerTest extends BaseCardTest {

    private static Card createCreature(String name, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    private static Card createCard(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }

    private void finishAnyReorder() {
        var reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        if (reorder != null) {
            harness.getGameService().handleInteractionAnswer(gd, player1,
                    new InteractionAnswer.CardOrder(IntStream.range(0, reorder.cards().size()).boxed().toList()));
        }
    }

    private void castMessenger() {
        harness.setHand(player1, List.of(new SylvanMessenger()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();  // resolve Sylvan Messenger -> ETB trigger queued
        harness.passBothPriorities();  // resolve the reveal trigger
    }

    @Test
    @DisplayName("Elf cards among the top four go to hand, the rest to the bottom")
    void elvesGoToHand() {
        Card elf1 = createCreature("Llanowar Elves", CardSubtype.ELF);
        Card elf2 = createCreature("Elvish Mystic", CardSubtype.ELF);
        Card bear = createCreature("Grizzly Bears", CardSubtype.BEAR);
        Card forest = createCard("Forest", CardType.LAND);
        Card shock = createCard("Shock", CardType.INSTANT);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(elf1, elf2, bear, forest, shock));

        castMessenger();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(elf1, elf2);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bear, forest);
        assertThat(deck).contains(bear, forest, shock);
    }

    @Test
    @DisplayName("Only the top four cards are revealed — a fifth Elf stays in the library")
    void onlyTopFourAreRevealed() {
        Card forest1 = createCard("Forest", CardType.LAND);
        Card forest2 = createCard("Forest", CardType.LAND);
        Card forest3 = createCard("Forest", CardType.LAND);
        Card forest4 = createCard("Forest", CardType.LAND);
        Card deepElf = createCreature("Elvish Mystic", CardSubtype.ELF);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(forest1, forest2, forest3, forest4, deepElf));

        castMessenger();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(deepElf);
        assertThat(deck).contains(deepElf);
    }

    @Test
    @DisplayName("A changeling card counts as an Elf card")
    void changelingCountsAsElf() {
        Card changeling = createCreature("Woodland Changeling");
        changeling.setKeywords(Set.of(Keyword.CHANGELING));
        Card shock = createCard("Shock", CardType.INSTANT);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(changeling, shock));

        castMessenger();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(changeling);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(shock);
    }

    @Test
    @DisplayName("A non-creature Elf card is still put into hand")
    void nonCreatureElfCardGoesToHand() {
        Card elfTribal = createCard("Elvish Promenade", CardType.SORCERY);
        elfTribal.setSubtypes(List.of(CardSubtype.ELF));

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(elfTribal));

        castMessenger();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(elfTribal);
    }
}
