package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProvidenceTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Providence sets its controller's life total to 26")
    void castingSetsControllerLifeTotal() {
        harness.setLife(player1, 7);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Providence()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Revealing Providence from the opening hand sets life to 26 at the first upkeep")
    void openingHandRevealSetsLifeTotal() {
        GameTestHarness openingHarness = new GameTestHarness();
        Player openingPlayer = openingHarness.getPlayer1();
        openingHarness.setLife(openingPlayer, 9);
        openingHarness.setHand(openingPlayer, List.of(new Providence()));
        openingHarness.skipMulligan();

        openingHarness.passBothPriorities();
        openingHarness.handleMayAbilityChosen(openingPlayer, true);

        assertThat(openingHarness.getGameData().playerLifeTotals.get(openingPlayer.getId())).isEqualTo(26);
    }

    @Test
    @DisplayName("Declining Providence's opening hand reveal leaves life unchanged")
    void decliningOpeningHandRevealLeavesLifeUnchanged() {
        GameTestHarness openingHarness = new GameTestHarness();
        Player openingPlayer = openingHarness.getPlayer1();
        openingHarness.setLife(openingPlayer, 9);
        openingHarness.setHand(openingPlayer, List.of(new Providence()));
        openingHarness.skipMulligan();

        openingHarness.passBothPriorities();
        openingHarness.handleMayAbilityChosen(openingPlayer, false);

        assertThat(openingHarness.getGameData().playerLifeTotals.get(openingPlayer.getId())).isEqualTo(9);
    }
}
