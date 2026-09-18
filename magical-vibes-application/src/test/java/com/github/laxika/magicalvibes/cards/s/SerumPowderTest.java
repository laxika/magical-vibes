package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerumPowder.class, DarksteelCitadel.class})
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
    void mulliganActionExilesHandAndDrawsSameNumber() throws Exception {
        GameTestHarness mulliganHarness = new GameTestHarness();
        Player player = mulliganHarness.getPlayer1();
        GameData gameData = mulliganHarness.getGameData();
        UUID previousDecisionId = gameData.playerMulliganDecisionIds.get(player.getId());
        List<GameEventFact> emittedFacts = new ArrayList<>();
        SerumPowder serumPowder = new SerumPowder();
        Card handCard = new DarksteelCitadel();
        Card libraryCardOne = new DarksteelCitadel();
        Card libraryCardTwo = new DarksteelCitadel();

        mulliganHarness.setHand(player, List.of(serumPowder, handCard));
        mulliganHarness.setLibrary(player, List.of(libraryCardOne, libraryCardTwo));

        try (AutoCloseable ignored = mulliganHarness.subscribeToGameEvents(batch ->
                batch.events().forEach(envelope -> emittedFacts.add(envelope.fact())))) {
            mulliganHarness.getGameService().mulligan(gameData, player);

            assertThat(gameData.interaction.isAwaitingInput()).isTrue();
            mulliganHarness.handleMayAbilityChosen(player, true);
        }

        assertThat(gameData.getPlayerExiledCards(player.getId())).containsExactly(serumPowder, handCard);
        assertThat(gameData.playerHands.get(player.getId())).containsExactly(libraryCardOne, libraryCardTwo);
        assertThat(gameData.playerDecks.get(player.getId())).isEmpty();
        assertThat(gameData.mulliganCounts).containsEntry(player.getId(), 0);
        assertThat(gameData.status).isEqualTo(GameStatus.MULLIGAN);
        UUID nextDecisionId = gameData.playerMulliganDecisionIds.get(player.getId());
        assertThat(nextDecisionId).isNotNull().isNotEqualTo(previousDecisionId);
        assertThat(emittedFacts).filteredOn(GameEventFact.DecisionRequested.class::isInstance)
                .extracting(GameEventFact.DecisionRequested.class::cast)
                .anySatisfy(decision -> {
                    assertThat(decision.decisionId()).isEqualTo(nextDecisionId);
                    assertThat(decision.decidingPlayerId()).isEqualTo(player.getId());
                    assertThat(decision.decisionKind()).isEqualTo(GameEventFact.DecisionKind.MULLIGAN);
                });
    }

    @Test
    @DisplayName("declining the mulligan action takes a normal mulligan")
    void decliningMulliganActionTakesNormalMulligan() {
        GameTestHarness mulliganHarness = new GameTestHarness();
        Player player = mulliganHarness.getPlayer1();
        GameData gameData = mulliganHarness.getGameData();

        mulliganHarness.setHand(player, List.of(new SerumPowder(),
                new DarksteelCitadel()));
        mulliganHarness.setLibrary(player, List.of(
                new DarksteelCitadel(),
                new DarksteelCitadel(),
                new DarksteelCitadel(),
                new DarksteelCitadel(),
                new DarksteelCitadel(),
                new DarksteelCitadel(),
                new DarksteelCitadel()));

        mulliganHarness.getGameService().mulligan(gameData, player);
        mulliganHarness.handleMayAbilityChosen(player, false);

        assertThat(gameData.getPlayerExiledCards(player.getId())).isEmpty();
        assertThat(gameData.playerHands.get(player.getId())).hasSize(7);
        assertThat(gameData.mulliganCounts).containsEntry(player.getId(), 1);
        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("using Serum Powder does not consume a mulligan")
    void usingSerumPowderStillAllowsNormalMulligan() {
        GameTestHarness mulliganHarness = new GameTestHarness();
        Player player = mulliganHarness.getPlayer1();
        GameData gameData = mulliganHarness.getGameData();
        SerumPowder serumPowder = new SerumPowder();
        DarksteelCitadel handCard = new DarksteelCitadel();
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            library.add(new DarksteelCitadel());
        }

        mulliganHarness.setHand(player, List.of(serumPowder, handCard));
        mulliganHarness.setLibrary(player, library);

        mulliganHarness.getGameService().mulligan(gameData, player);
        mulliganHarness.handleMayAbilityChosen(player, true);

        assertThat(gameData.mulliganCounts).containsEntry(player.getId(), 0);
        mulliganHarness.getGameService().mulligan(gameData, player);

        assertThat(gameData.getPlayerExiledCards(player.getId())).containsExactly(serumPowder, handCard);
        assertThat(gameData.playerHands.get(player.getId())).hasSize(7);
        assertThat(gameData.playerDecks.get(player.getId())).isEmpty();
        assertThat(gameData.mulliganCounts).containsEntry(player.getId(), 1);
        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("a Serum Powder drawn after using one can be used as well")
    void drawnSerumPowderCanBeUsedAfterAnEarlierPowder() {
        GameTestHarness mulliganHarness = new GameTestHarness();
        Player player = mulliganHarness.getPlayer1();
        GameData gameData = mulliganHarness.getGameData();
        SerumPowder firstSerumPowder = new SerumPowder();
        SerumPowder secondSerumPowder = new SerumPowder();
        DarksteelCitadel firstHandCard = new DarksteelCitadel();
        DarksteelCitadel firstDrawnCard = new DarksteelCitadel();
        DarksteelCitadel secondDrawnCard = new DarksteelCitadel();
        DarksteelCitadel thirdDrawnCard = new DarksteelCitadel();
        List<Card> library = new ArrayList<>(List.of(
                secondSerumPowder, firstDrawnCard, secondDrawnCard, thirdDrawnCard));
        for (int i = 0; i < 7; i++) {
            library.add(new DarksteelCitadel());
        }

        mulliganHarness.setHand(player, List.of(firstSerumPowder, firstHandCard));
        mulliganHarness.setLibrary(player, library);

        mulliganHarness.getGameService().mulligan(gameData, player);
        mulliganHarness.handleMayAbilityChosen(player, true);
        mulliganHarness.getGameService().mulligan(gameData, player);
        mulliganHarness.handleMayAbilityChosen(player, true);

        assertThat(gameData.getPlayerExiledCards(player.getId()))
                .containsExactly(firstSerumPowder, firstHandCard, secondSerumPowder, firstDrawnCard);
        assertThat(gameData.playerHands.get(player.getId()))
                .containsExactly(secondDrawnCard, thirdDrawnCard);
        assertThat(gameData.mulliganCounts).containsEntry(player.getId(), 0);
        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
    }
}
