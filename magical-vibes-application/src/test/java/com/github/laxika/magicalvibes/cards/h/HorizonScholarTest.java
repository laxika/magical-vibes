package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HorizonScholar.class})
class HorizonScholarTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Horizon Scholar enters the battlefield and offers scry 2")
    void etbOffersScryTwo() {
        harness.castFromHand(player1, new HorizonScholar(), "{5}{U}");
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB trigger

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Horizon Scholar");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Scry 2 keeping both on top preserves library order")
    void scryBothOnTop() {
        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop0 = deck.get(0);
        Card originalTop1 = deck.get(1);

        harness.castFromHand(player1, new HorizonScholar(), "{5}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(deck.get(0)).isSameAs(originalTop0);
        assertThat(deck.get(1)).isSameAs(originalTop1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry 2 splitting top and bottom reorders the library")
    void scrySplitTopAndBottom() {
        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop0 = deck.get(0);
        Card originalTop1 = deck.get(1);

        harness.castFromHand(player1, new HorizonScholar(), "{5}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(deck.get(0)).isSameAs(originalTop1);
        assertThat(deck.get(deck.size() - 1)).isSameAs(originalTop0);
    }

    @Test
    @DisplayName("Scry 2 can reverse both cards on top")
    void reversesTopCards() {
        Card first = new HorizonScholar();
        Card second = new HorizonScholar();
        Card third = new HorizonScholar();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.castFromHand(player1, new HorizonScholar(), "{5}{U}");
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry 2 can put both cards on bottom in either order")
    void reversesBottomCards() {
        Card first = new HorizonScholar();
        Card second = new HorizonScholar();
        Card third = new HorizonScholar();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.castFromHand(player1, new HorizonScholar(), "{5}{U}");
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, second, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry 2 with one card looks at only that card")
    void scriesOneCardFromShortLibrary() {
        Card onlyCard = new HorizonScholar();
        harness.setLibrary(player1, List.of(onlyCard));

        harness.castFromHand(player1, new HorizonScholar(), "{5}{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry 2 with an empty library finishes without a choice or a draw")
    void emptyLibraryFinishesWithoutInteraction() {
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new HorizonScholar(), "{5}{U}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Horizon Scholar");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The trigger scries its controller's library")
    void otherPlayerScriesTheirOwnLibrary() {
        Card first = new HorizonScholar();
        Card second = new HorizonScholar();
        Card opponentCard = new HorizonScholar();
        harness.setLibrary(player2, List.of(first, second));
        harness.setLibrary(player1, List.of(opponentCard));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new HorizonScholar(), "{5}{U}");
        resolveAllTriggers();

        PendingInteraction.Scry interaction = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(interaction.playerId()).isEqualTo(player2.getId());
        assertThat(interaction.cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second, first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentCard);
    }

}
