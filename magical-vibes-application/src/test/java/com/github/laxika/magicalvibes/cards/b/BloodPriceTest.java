package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodPrice.class})
class BloodPriceTest extends BaseCardTest {

    @Test
    void keepsTwoCardsAndReordersTheRestOnTheBottomThenLosesLife() {
        Card first = new BloodPrice();
        Card second = new BloodPrice();
        Card third = new BloodPrice();
        Card fourth = new BloodPrice();
        Card untouched = new BloodPrice();
        BloodPrice spell = new BloodPrice();
        harness.setLibrary(player1, List.of(first, second, third, fourth, untouched));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(first, second, third, fourth);
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.reorderRemainingToBottom()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), third.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, third);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> remaining = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(
                List.of(remaining.indexOf(fourth), remaining.indexOf(second))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, fourth, second);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    void withFewerThanTwoCardsAllAvailableCardsGoToHandAndLifeIsStillLost() {
        Card only = new BloodPrice();
        harness.setLibrary(player1, List.of(only));
        harness.setHand(player1, List.of(new BloodPrice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(only);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryStillLosesLifeWithoutDrawing() {
        BloodPrice spell = new BloodPrice();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void exactlyTwoCardsBothGoToHandWithoutAChoice() {
        Card first = new BloodPrice();
        Card second = new BloodPrice();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new BloodPrice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void threeCardLibraryKeepsTwoAndBottomsTheSingleRemainingCard() {
        Card first = new BloodPrice();
        Card second = new BloodPrice();
        Card third = new BloodPrice();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new BloodPrice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId(), third.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
