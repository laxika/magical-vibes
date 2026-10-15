package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OtherworldlyGaze.class, Island.class})
class OtherworldlyGazeTest extends BaseCardTest {

    @Test
    void surveilsThreeCards() {
        OtherworldlyGaze gaze = new OtherworldlyGaze();
        Card first = new Island();
        Card second = new Island();
        Card third = new Island();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(gaze));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(first, second, third);
        assertThat(surveil.toGraveyard()).isTrue();

        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1, 2)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third, gaze);
    }

    @Test
    void flashbackExilesTheCardAfterResolving() {
        OtherworldlyGaze gaze = new OtherworldlyGaze();
        Card first = new Island();
        Card second = new Island();
        Card third = new Island();
        harness.setGraveyard(player1, List.of(gaze));
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1, 2)));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(gaze);
    }

    @Test
    void canKeepAllCardsInAnyOrderWithoutTouchingTheFourth() {
        OtherworldlyGaze gaze = new OtherworldlyGaze();
        Card first = new Island();
        Card second = new Island();
        Card third = new Island();
        Card fourth = new Island();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(gaze));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(2, 0, 1), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, first, second, fourth);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(gaze);
    }

    @Test
    void canMillSomeCardsAndReorderTheRest() {
        OtherworldlyGaze gaze = new OtherworldlyGaze();
        Card first = new Island();
        Card second = new Island();
        Card third = new Island();
        Card fourth = new Island();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(gaze));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(2, 0), List.of(1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, first, fourth);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second, gaze);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void surveilsAllAvailableCardsInAShortLibrary(int librarySize) {
        OtherworldlyGaze gaze = new OtherworldlyGaze();
        List<Card> cards = List.<Card>of(new Island(), new Island()).subList(0, librarySize);
        harness.setLibrary(player1, cards);
        harness.setHand(player1, List.of(gaze));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactlyElementsOf(cards);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(librarySize == 1 ? List.of(0) : List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(
                librarySize == 1 ? cards : List.of(cards.get(1), cards.get(0)));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(gaze);
    }

    @Test
    void resolvesWithAnEmptyLibraryWithoutRequestingAChoice() {
        OtherworldlyGaze gaze = new OtherworldlyGaze();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(gaze));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(gaze);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canFlashbackDuringOpponentsTurnAndSurveilsItsControllersLibrary() {
        OtherworldlyGaze gaze = new OtherworldlyGaze();
        Card first = new Island();
        Card second = new Island();
        Card third = new Island();
        Card opponentCard = new Island();
        harness.setGraveyard(player2, List.of(gaze));
        harness.setLibrary(player2, List.of(first, second, third));
        harness.setLibrary(player1, List.of(opponentCard));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.castFlashback(player2, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(2, 1), List.of(0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(gaze);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void flashbackRequiresTheAdditionalGenericMana() {
        OtherworldlyGaze gaze = new OtherworldlyGaze();
        harness.setGraveyard(player1, List.of(gaze));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(gaze);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
