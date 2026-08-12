package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SerumPowderTest extends BaseCardTest {

    @Test
    @DisplayName("tap ability adds one colorless mana")
    void tapAbilityAddsColorlessMana() {
        harness.addToBattlefield(player1, new SerumPowder());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("mulligan action exiles the hand and draws the same number of cards")
    void mulliganActionExilesHandAndDrawsSameNumber() {
        GameTestHarness mulliganHarness = new GameTestHarness();
        Player player = mulliganHarness.getPlayer1();
        GameData gameData = mulliganHarness.getGameData();
        SerumPowder serumPowder = new SerumPowder();
        Card handCard = new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        Card libraryCardOne = new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        Card libraryCardTwo = new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();

        mulliganHarness.setHand(player, List.of(serumPowder, handCard));
        mulliganHarness.setLibrary(player, List.of(libraryCardOne, libraryCardTwo));

        mulliganHarness.getGameService().mulligan(gameData, player);

        assertThat(gameData.interaction.isAwaitingInput()).isTrue();
        mulliganHarness.handleMayAbilityChosen(player, true);

        assertThat(gameData.getPlayerExiledCards(player.getId())).containsExactly(serumPowder, handCard);
        assertThat(gameData.playerHands.get(player.getId())).containsExactly(libraryCardOne, libraryCardTwo);
        assertThat(gameData.playerDecks.get(player.getId())).isEmpty();
        assertThat(gameData.mulliganCounts).containsEntry(player.getId(), 0);
        assertThat(gameData.status).isEqualTo(GameStatus.MULLIGAN);
    }

    @Test
    @DisplayName("declining the mulligan action takes a normal mulligan")
    void decliningMulliganActionTakesNormalMulligan() {
        GameTestHarness mulliganHarness = new GameTestHarness();
        Player player = mulliganHarness.getPlayer1();
        GameData gameData = mulliganHarness.getGameData();

        mulliganHarness.setHand(player, List.of(new SerumPowder(),
                new com.github.laxika.magicalvibes.cards.g.GrizzlyBears()));
        mulliganHarness.setLibrary(player, List.of(
                new com.github.laxika.magicalvibes.cards.g.GrizzlyBears(),
                new com.github.laxika.magicalvibes.cards.g.GrizzlyBears(),
                new com.github.laxika.magicalvibes.cards.g.GrizzlyBears(),
                new com.github.laxika.magicalvibes.cards.g.GrizzlyBears(),
                new com.github.laxika.magicalvibes.cards.g.GrizzlyBears(),
                new com.github.laxika.magicalvibes.cards.g.GrizzlyBears(),
                new com.github.laxika.magicalvibes.cards.g.GrizzlyBears()));

        mulliganHarness.getGameService().mulligan(gameData, player);
        mulliganHarness.handleMayAbilityChosen(player, false);

        assertThat(gameData.getPlayerExiledCards(player.getId())).isEmpty();
        assertThat(gameData.playerHands.get(player.getId())).hasSize(7);
        assertThat(gameData.mulliganCounts).containsEntry(player.getId(), 1);
        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
    }
}
