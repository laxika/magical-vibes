package com.github.laxika.magicalvibes.cards.g;

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

class GoblinRingleaderTest extends BaseCardTest {

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

    private void castRingleader() {
        harness.setHand(player1, List.of(new GoblinRingleader()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Goblin cards among the top four go to hand, the rest go to the bottom")
    void goblinsGoToHand() {
        Card goblin1 = createCreature("Goblin token", CardSubtype.GOBLIN);
        Card goblin2 = createCreature("Goblin Raider", CardSubtype.GOBLIN);
        Card bear = createCreature("Grizzly Bears", CardSubtype.BEAR);
        Card forest = createCard("Forest", CardType.LAND);
        Card shock = createCard("Shock", CardType.INSTANT);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(goblin1, goblin2, bear, forest, shock));

        castRingleader();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(goblin1, goblin2);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bear, forest);
        assertThat(deck).contains(bear, forest, shock);
    }

    @Test
    @DisplayName("Only the top four cards are revealed")
    void onlyTopFourAreRevealed() {
        Card forest1 = createCard("Forest", CardType.LAND);
        Card forest2 = createCard("Forest", CardType.LAND);
        Card forest3 = createCard("Forest", CardType.LAND);
        Card forest4 = createCard("Forest", CardType.LAND);
        Card deepGoblin = createCreature("Goblin Raider", CardSubtype.GOBLIN);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(forest1, forest2, forest3, forest4, deepGoblin));

        castRingleader();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(deepGoblin);
        assertThat(deck).contains(deepGoblin);
    }

    @Test
    @DisplayName("A changeling card counts as a Goblin card")
    void changelingCountsAsGoblin() {
        Card changeling = createCreature("Woodland Changeling");
        changeling.setKeywords(Set.of(Keyword.CHANGELING));
        Card shock = createCard("Shock", CardType.INSTANT);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(changeling, shock));

        castRingleader();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(changeling);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(shock);
    }

    @Test
    @DisplayName("A noncreature Goblin card is still put into hand")
    void nonCreatureGoblinCardGoesToHand() {
        Card goblinTribal = createCard("Goblin Grenade", CardType.SORCERY);
        goblinTribal.setSubtypes(List.of(CardSubtype.GOBLIN));

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(goblinTribal));

        castRingleader();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(goblinTribal);
    }
}
