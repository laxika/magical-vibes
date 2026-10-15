package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Providence.class})
class ProvidenceTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Providence sets its controller's life total to 26")
    void castingSetsControllerLifeTotal() {
        harness.setLife(player1, 7);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Providence()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 26);
        harness.assertLife(player2, 20);
        assertThat(gd.lifeGainedThisTurn.get(player1.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Providence");
    }

    @Test
    @DisplayName("Revealing Providence before the first turn sets life to 26 at the first upkeep")
    void openingHandRevealSetsLifeTotal() {
        GameTestHarness openingHarness = new GameTestHarness();
        Player openingPlayer = openingHarness.getPlayer1();
        openingHarness.getGameData().alwaysOfferPriorityWindows = true;
        openingHarness.setLife(openingPlayer, 9);
        openingHarness.setHand(openingPlayer, List.of(new Providence()));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().status).isEqualTo(GameStatus.MULLIGAN);
        openingHarness.handleMayAbilityChosen(openingPlayer, true);
        openingHarness.assertLife(openingPlayer, 9);
        openingHarness.assertInHand(openingPlayer, "Providence");
        openingHarness.passBothPriorities();

        openingHarness.assertLife(openingPlayer, 26);
        openingHarness.assertLife(openingHarness.getPlayer2(), 20);
        assertThat(openingHarness.getGameData().lifeGainedThisTurn.get(openingPlayer.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Declining Providence's pregame reveal leaves life unchanged and creates no trigger")
    void decliningOpeningHandRevealLeavesLifeUnchanged() {
        GameTestHarness openingHarness = new GameTestHarness();
        Player openingPlayer = openingHarness.getPlayer1();
        openingHarness.getGameData().alwaysOfferPriorityWindows = true;
        openingHarness.setLife(openingPlayer, 9);
        openingHarness.setHand(openingPlayer, List.of(new Providence()));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().status).isEqualTo(GameStatus.MULLIGAN);
        openingHarness.handleMayAbilityChosen(openingPlayer, false);

        openingHarness.assertLife(openingPlayer, 9);
        assertThat(openingHarness.getGameData().stack).isEmpty();
        openingHarness.assertInHand(openingPlayer, "Providence");
    }

    @Test
    @DisplayName("Casting Providence lowers a life total above 26 without gaining life")
    void castingLowersLifeTotal() {
        harness.setLife(player1, 40);
        harness.setHand(player1, List.of(new Providence()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 26);
        harness.assertLife(player2, 20);
        assertThat(gd.lifeGainedThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Casting Providence at 26 life does not gain life")
    void castingAtTwentySixDoesNotGainLife() {
        harness.setLife(player1, 26);
        harness.setHand(player1, List.of(new Providence()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 26);
        assertThat(gd.lifeGainedThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("The nonstarting player's revealed Providence triggers on the starting player's first upkeep")
    void nonstartingPlayerGetsFirstUpkeepTrigger() {
        GameTestHarness openingHarness = new GameTestHarness();
        Player openingPlayer = openingHarness.getPlayer2();
        openingHarness.getGameData().alwaysOfferPriorityWindows = true;
        openingHarness.setLife(openingPlayer, 40);
        openingHarness.setHand(openingPlayer, List.of(new Providence()));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().status).isEqualTo(GameStatus.MULLIGAN);
        openingHarness.handleMayAbilityChosen(openingPlayer, true);
        openingHarness.assertLife(openingPlayer, 40);
        assertThat(openingHarness.getGameData().activePlayerId).isEqualTo(openingHarness.getPlayer1().getId());
        openingHarness.passBothPriorities();

        openingHarness.assertLife(openingPlayer, 26);
        openingHarness.assertLife(openingHarness.getPlayer1(), 20);
    }
}
