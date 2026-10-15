package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChartACourse.class, Opt.class})
class ChartACourseTest extends BaseCardTest {

    @Test
    @DisplayName("When player did not attack this turn, draws 2 then must discard 1")
    void noAttack_mustDiscard() {

        harness.castFromHand(player1, new ChartACourse(), "{1}{U}");
        harness.passBothPriorities();

        // Should be awaiting discard choice (did not attack)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class) != null).isTrue();

        // Hand should have 2 cards (0 original after casting + 2 drawn)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        // Choose to discard the first card
        harness.handleCardChosen(player1, 0);

        // After discard, hand should be 1
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("When player attacked this turn, draws 2 and does not have to discard")
    void attacked_noDiscard() {

        // Mark player1 as having attacked this turn
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        harness.castFromHand(player1, new ChartACourse(), "{1}{U}");
        harness.passBothPriorities();

        // Should NOT be awaiting discard — attack condition met
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class) != null).isFalse();

        // Hand should have 2 cards (0 original after casting + 2 drawn)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("When player attacked this turn and has other cards, all are kept")
    void attacked_withOtherCards_allKept() {
        Card otherCard = new Opt();
        harness.setHand(player1, List.of(new ChartACourse(), otherCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Mark player1 as having attacked this turn
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);

        // Hand should have 3 cards (1 remaining + 2 drawn)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class) != null).isFalse();
    }

    @Test
    @DisplayName("Chart a Course goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {

        // Attacked so no discard interaction needed
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        harness.castFromHand(player1, new ChartACourse(), "{1}{U}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chart a Course");
    }

    @Test
    @DisplayName("An opponent attacking does not waive the controller's discard")
    void opponentsAttackDoesNotPreventDiscard() {
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        harness.setLibrary(player1, List.of(new Opt(), new Opt()));

        harness.castFromHand(player1, new ChartACourse(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Opt");
        harness.assertInGraveyard(player1, "Chart a Course");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    @Test
    @DisplayName("The controller can discard a card already in hand after drawing two")
    void canDiscardExistingCardAfterDrawing() {
        Card existingCard = new Opt();
        Card firstDraw = new ChartACourse();
        Card secondDraw = new Opt();
        harness.setHand(player1, List.of(new ChartACourse(), existingCard));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(existingCard, firstDraw, secondDraw);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(existingCard);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }
}
