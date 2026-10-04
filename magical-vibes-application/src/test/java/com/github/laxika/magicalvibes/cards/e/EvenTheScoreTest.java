package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EvenTheScore.class, Divination.class, GrizzlyBears.class})
class EvenTheScoreTest extends BaseCardTest {

    @Test
    void drawsXCards() {
        harness.setHand(player1, List.of(new EvenTheScore()));
        harness.setLibrary(player1, cards(10));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Even the Score");
    }

    @Test
    void costsLessAfterOpponentDrawsFourCards() {
        harness.setHand(player1, List.of(new EvenTheScore()));
        harness.setHand(player2, List.of(new Divination(), new Divination()));
        harness.setLibrary(player2, cards(10));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        harness.passPriority(player2);
        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Even the Score");
    }

    @Test
    void doesNotReduceCostBeforeOpponentDrawsFourCards() {
        harness.setHand(player1, List.of(new EvenTheScore()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5})
    void reducedCostStillRequiresAndDrawsChosenX(int opponentDraws) {
        drawWithEvenTheScore(player2, opponentDraws);
        harness.setHand(player1, List.of(new EvenTheScore()));
        harness.setLibrary(player1, List.of(new EvenTheScore(), new EvenTheScore(), new EvenTheScore()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player1);

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Even the Score");
    }

    @Test
    void threeOpponentDrawsDoNotReduceCost() {
        drawWithEvenTheScore(player2, 3);
        harness.setHand(player1, List.of(new EvenTheScore()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Even the Score");
    }

    @Test
    void controllerDrawingFourCardsDoesNotReduceCost() {
        drawWithEvenTheScore(player1, 4);
        harness.setHand(player1, List.of(new EvenTheScore()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Even the Score");
    }

    @Test
    void reductionDoesNotPayForX() {
        drawWithEvenTheScore(player2, 4);
        harness.setHand(player1, List.of(new EvenTheScore()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Even the Score");
    }

    @Test
    void zeroXWithoutReductionDrawsNothing() {
        harness.setHand(player1, List.of(new EvenTheScore()));
        harness.setLibrary(player1, List.of(new EvenTheScore()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Even the Score");
    }

    private void drawWithEvenTheScore(Player player, int count) {
        harness.setHand(player, List.of(new EvenTheScore()));
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < count + 2; i++) {
            library.add(new EvenTheScore());
        }
        harness.setLibrary(player, library);
        harness.addMana(player, ManaColor.BLUE, 3);
        harness.addMana(player, ManaColor.COLORLESS, count);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player, 0, count, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player.getId())).hasSize(count);
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
