package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GoblinLegionnaire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhirlpoolDrake.class, GoblinLegionnaire.class})
class WhirlpoolDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Its enters-the-battlefield ability wheels only its controller's hand")
    void entersWheelsOnlyItsControllersHand() {
        harness.setHand(player1, List.of(new WhirlpoolDrake(), new GoblinLegionnaire()));
        harness.setHand(player2, List.of(new GoblinLegionnaire(), new GoblinLegionnaire()));
        harness.setLibrary(player1, libraryWithThreeCards());
        harness.setLibrary(player2, libraryWithThreeCards());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        String playerOneName = gd.playerIdToName.get(player1.getId());
        String playerTwoName = gd.playerIdToName.get(player2.getId());
        assertThat(gameLogContains(playerOneName + " shuffles 1 card from hand into their library.")).isTrue();
        assertThat(gameLogContains(playerOneName + " draws 1 card.")).isTrue();
        assertThat(gd.gameLog).noneMatch(entry -> entry.plainText().contains(playerTwoName + " shuffles"));
        assertThat(gd.gameLog).noneMatch(entry -> entry.plainText().contains(playerTwoName + " draws"));
    }

    @Test
    @DisplayName("Its enters-the-battlefield ability draws nothing when its controller has no cards in hand")
    void entersWithEmptyHandDoesNotDraw() {
        harness.setHand(player1, List.of(new WhirlpoolDrake()));
        harness.setHand(player2, List.of(new GoblinLegionnaire()));
        harness.setLibrary(player1, libraryWithThreeCards());
        harness.setLibrary(player2, libraryWithThreeCards());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gameLogContains("has no cards in hand to shuffle")).isTrue();
        String playerTwoName = gd.playerIdToName.get(player2.getId());
        assertThat(gd.gameLog).noneMatch(entry -> entry.plainText().contains(playerTwoName + " shuffles"));
        assertThat(gd.gameLog).noneMatch(entry -> entry.plainText().contains(playerTwoName + " draws"));
    }

    @Test
    @DisplayName("Its death ability wheels only its controller's hand")
    void diesWheelsOnlyItsControllersHand() {
        harness.setHand(player1, List.of(new GoblinLegionnaire()));
        harness.setHand(player2, List.of(new GoblinLegionnaire(), new GoblinLegionnaire()));
        harness.setLibrary(player1, libraryWithThreeCards());
        harness.setLibrary(player2, libraryWithThreeCards());

        Permanent drake = harness.addToBattlefieldAndReturn(player1, new WhirlpoolDrake());
        harness.addToBattlefield(player2, new GoblinLegionnaire());
        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player2, 0, 0, null, drake.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Whirlpool Drake");
        String playerOneName = gd.playerIdToName.get(player1.getId());
        String playerTwoName = gd.playerIdToName.get(player2.getId());
        assertThat(gameLogContains(playerOneName + " shuffles 1 card from hand into their library.")).isTrue();
        assertThat(gameLogContains(playerOneName + " draws 1 card.")).isTrue();
        assertThat(gd.gameLog).noneMatch(entry -> entry.plainText().contains(playerTwoName + " shuffles"));
        assertThat(gd.gameLog).noneMatch(entry -> entry.plainText().contains(playerTwoName + " draws"));
    }

    @Test
    @DisplayName("Its enters ability uses the hand size when the trigger resolves")
    void entersUsesHandSizeAtResolution() {
        harness.setHand(player1, List.of(new WhirlpoolDrake(), new GoblinLegionnaire()));
        harness.setLibrary(player1, libraryWithThreeCards());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new GoblinLegionnaire(), new GoblinLegionnaire()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        String playerName = gd.playerIdToName.get(player1.getId());
        assertThat(gameLogContains(playerName + " shuffles 2 cards from hand into their library.")).isTrue();
        assertThat(gameLogContains(playerName + " draws 2 cards.")).isTrue();
    }

    @Test
    @DisplayName("Its death ability shuffles the hand before drawing from an empty library")
    void diesWithEmptyLibraryDrawsShuffledHand() {
        Card first = new GoblinLegionnaire();
        Card second = new GoblinLegionnaire();
        harness.setHand(player1, List.of(first, second));
        harness.setLibrary(player1, List.of());
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new WhirlpoolDrake());
        harness.addToBattlefield(player2, new GoblinLegionnaire());
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player2, 0, 0, null, drake.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Whirlpool Drake");
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        String playerName = gd.playerIdToName.get(player1.getId());
        assertThat(gameLogContains(playerName + " shuffles 2 cards from hand into their library.")).isTrue();
        assertThat(gameLogContains(playerName + " draws 2 cards.")).isTrue();
    }

    private List<Card> libraryWithThreeCards() {
        return List.of(new GoblinLegionnaire(), new GoblinLegionnaire(), new GoblinLegionnaire());
    }
}
