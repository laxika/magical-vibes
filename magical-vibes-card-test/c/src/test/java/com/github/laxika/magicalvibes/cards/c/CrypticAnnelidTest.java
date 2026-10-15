package com.github.laxika.magicalvibes.cards.c;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed(CrypticAnnelid.class)
class CrypticAnnelidTest extends BaseCardTest {

    @Test
    @DisplayName("ETB scries 1, then 2, then 3")
    void etbScriesOneThenTwoThenThree() {
        Card first = new CrypticAnnelid();
        Card second = new CrypticAnnelid();
        Card third = new CrypticAnnelid();
        Card fourth = new CrypticAnnelid();
        Card fifth = new CrypticAnnelid();
        Card sixth = new CrypticAnnelid();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth, sixth));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new CrypticAnnelid(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scryOne = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scryOne.cards()).containsExactly(first);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        PendingInteraction.Scry scryTwo = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scryTwo.cards()).containsExactly(second, third);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        PendingInteraction.Scry scryThree = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scryThree.cards()).containsExactly(third, fourth, fifth);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(2, 0, 1), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth, third, fourth, sixth, first, second);
    }

    @Test
    @DisplayName("ETB completes all three scries with an empty library")
    void etbCompletesAllThreeScriesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new CrypticAnnelid(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("scries 1")).isTrue();
        assertThat(gameLogContains("scries 2")).isTrue();
        assertThat(gameLogContains("scries 3")).isTrue();
    }

    @Test
    @DisplayName("Later scries revisit bottomed cards when the library has only two cards")
    void laterScriesRevisitBottomedCardsInShortLibrary() {
        Card first = new CrypticAnnelid();
        Card second = new CrypticAnnelid();
        Card opponentCard = new CrypticAnnelid();
        harness.setLibrary(player2, List.of(first, second));
        harness.setLibrary(player1, List.of(opponentCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new CrypticAnnelid(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scryOne = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scryOne.playerId()).isEqualTo(player2.getId());
        assertThat(scryOne.cards()).containsExactly(first);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        PendingInteraction.Scry scryTwo = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scryTwo.playerId()).isEqualTo(player2.getId());
        assertThat(scryTwo.cards()).containsExactly(second, first);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        PendingInteraction.Scry scryThree = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scryThree.playerId()).isEqualTo(player2.getId());
        assertThat(scryThree.cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second, first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentCard);
    }
}
