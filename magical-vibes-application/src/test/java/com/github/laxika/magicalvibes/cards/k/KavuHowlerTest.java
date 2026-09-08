package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(KavuHowler.class)
class KavuHowlerTest extends BaseCardTest {

    private static Card createCard(String name, CardType type, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    private void finishAnyReorder() {
        var reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        if (reorder != null) {
            harness.getGameService().handleInteractionAnswer(gd, player1,
                    new InteractionAnswer.CardOrder(IntStream.range(0, reorder.cards().size()).boxed().toList()));
        }
    }

    private void castHowler() {
        harness.setHand(player1, List.of(new KavuHowler()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Kavu cards among the top four go to hand and the rest go to the bottom")
    void kavuCardsGoToHand() {
        Card kavu1 = createCard("Kavu Climber", CardType.CREATURE, CardSubtype.KAVU);
        Card forest = createCard("Forest", CardType.LAND);
        Card kavu2 = createCard("Kavu Titan", CardType.CREATURE, CardSubtype.KAVU);
        Card shock = createCard("Shock", CardType.INSTANT);
        Card deepKavu = createCard("Kavu Scout", CardType.CREATURE, CardSubtype.KAVU);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(kavu1, forest, kavu2, shock, deepKavu));

        castHowler();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(kavu1, kavu2);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest, shock, deepKavu);
        assertThat(deck).contains(forest, shock, deepKavu);
    }

    @Test
    @DisplayName("Noncreature Kavu cards also go to hand")
    void noncreatureKavuCardsGoToHand() {
        Card kavuSpell = createCard("Kavu Research", CardType.SORCERY, CardSubtype.KAVU);
        Card forest = createCard("Forest", CardType.LAND);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(kavuSpell, forest));

        castHowler();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(kavuSpell);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
    }
}
