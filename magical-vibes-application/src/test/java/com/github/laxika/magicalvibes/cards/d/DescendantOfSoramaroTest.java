package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DescendantOfSoramaro.class, ArabaMothrider.class})
class DescendantOfSoramaroTest extends BaseCardTest {

    @Test
    @DisplayName("Reorders as many cards as are in the controller's hand")
    void reordersAsManyCardsAsAreInHand() {
        Permanent descendant = addCreatureReady(player1, new DescendantOfSoramaro());

        Card handCard1 = new ArabaMothrider();
        Card handCard2 = new ArabaMothrider();
        Card handCard3 = new ArabaMothrider();
        Card topCard = new ArabaMothrider();
        Card secondCard = new ArabaMothrider();
        Card thirdCard = new ArabaMothrider();
        Card fourthCard = new ArabaMothrider();
        harness.setHand(player1, List.of(handCard1, handCard2, handCard3));
        harness.setLibrary(player1, List.of(topCard, secondCard, thirdCard, fourthCard));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(topCard, secondCard, thirdCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(thirdCard, secondCard, topCard, fourthCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(descendant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Uses the hand size when the ability resolves")
    void usesHandSizeAtResolution() {
        addCreatureReady(player1, new DescendantOfSoramaro());
        List<Card> library = List.of(new ArabaMothrider(), new ArabaMothrider(), new ArabaMothrider());
        harness.setHand(player1, List.of(new ArabaMothrider(), new ArabaMothrider(), new ArabaMothrider()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new ArabaMothrider()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Reorders the entire library when it has fewer cards than the hand")
    void reordersShortLibrary() {
        addCreatureReady(player1, new DescendantOfSoramaro());
        List<Card> hand = List.of(new ArabaMothrider(), new ArabaMothrider(), new ArabaMothrider());
        Card topCard = new ArabaMothrider();
        Card secondCard = new ArabaMothrider();
        harness.setHand(player1, hand);
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(topCard, secondCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Leaves the library unchanged when the controller has no cards in hand")
    void doesNothingWithNoCardsInHand() {
        addCreatureReady(player1, new DescendantOfSoramaro());
        Card topCard = new ArabaMothrider();
        Card secondCard = new ArabaMothrider();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
