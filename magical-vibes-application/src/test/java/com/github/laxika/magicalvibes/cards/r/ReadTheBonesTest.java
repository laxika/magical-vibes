package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReadTheBones.class})
class ReadTheBonesTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Read the Bones pauses on a scry 2")
    void resolvingEntersScryState() {
        harness.setHand(player1, List.of(new ReadTheBones()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Keeping both scried cards on top draws them and loses 2 life")
    void scryKeepOnTopThenDrawTwoAndLoseTwoLife() {
        harness.setHand(player1, List.of(new ReadTheBones()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);
        int startingLife = gd.playerLifeTotals.get(player1.getId());
        int deckSize = deck.size();

        harness.castAndResolveInstant(player1, 0);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top0, top1);
        assertThat(deck).hasSize(deckSize - 2);
        harness.assertLife(player1, startingLife - 2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Putting both scried cards on the bottom draws the next two cards instead")
    void scryBottomDrawsDifferentCards() {
        harness.setHand(player1, List.of(new ReadTheBones()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);
        Card top2 = deck.get(2);
        Card top3 = deck.get(3);

        harness.castAndResolveInstant(player1, 0);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top2, top3);
        assertThat(deck).doesNotContain(top2, top3);
        assertThat(deck.get(deck.size() - 2)).isSameAs(top0);
        assertThat(deck.get(deck.size() - 1)).isSameAs(top1);
    }

    @Test
    @DisplayName("Scry can reverse the two cards before drawing them")
    void scryReordersTopCards() {
        Card first = new ReadTheBones();
        Card second = new ReadTheBones();
        harness.setLibrary(player1, List.of(first, second, new ReadTheBones()));
        harness.castFromHand(player1, new ReadTheBones(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, first);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry can keep one card on top and put the other on the bottom")
    void scrySplitsTopAndBottom() {
        Card first = new ReadTheBones();
        Card second = new ReadTheBones();
        Card third = new ReadTheBones();
        Card fourth = new ReadTheBones();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.castFromHand(player1, new ReadTheBones(), "{2}{B}");
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth, first);
        harness.assertLife(player1, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Bottoming both cards in a two-card library still draws them in the chosen order")
    void bottomedCardsCanStillBeDrawnFromShortLibrary() {
        Card first = new ReadTheBones();
        Card second = new ReadTheBones();
        harness.setLibrary(player1, List.of(first, second));
        harness.castFromHand(player1, new ReadTheBones(), "{2}{B}");
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
