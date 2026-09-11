package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodPrice.class, GrizzlyBears.class})
class BloodPriceTest extends BaseCardTest {

    @Test
    void keepsTwoCardsAndReordersTheRestOnTheBottomThenLosesLife() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card fourth = new GrizzlyBears();
        Card untouched = new GrizzlyBears();
        BloodPrice spell = new BloodPrice();
        harness.setLibrary(player1, List.of(first, second, third, fourth, untouched));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

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
        Card only = new GrizzlyBears();
        harness.setLibrary(player1, List.of(only));
        harness.setHand(player1, List.of(new BloodPrice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(only);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
