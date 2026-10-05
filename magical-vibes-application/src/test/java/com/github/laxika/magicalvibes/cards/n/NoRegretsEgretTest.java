package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.s.SerumPowder;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoRegretsEgret.class, DarksteelCitadel.class})
class NoRegretsEgretTest extends BaseCardTest {

    @Test
    @DisplayName("accepting the mulligan action reveals Egret and privately looks at the top two")
    void acceptingMulliganActionKeepsZonesAndOffersMulliganAgain() throws Exception {
        GameTestHarness mulliganHarness = new GameTestHarness();
        Player player = mulliganHarness.getPlayer1();
        GameData gameData = mulliganHarness.getGameData();
        NoRegretsEgret egret = new NoRegretsEgret();
        DarksteelCitadel handCard = new DarksteelCitadel();
        DarksteelCitadel topCard = new DarksteelCitadel();
        DarksteelCitadel secondCard = new DarksteelCitadel();
        List<GameEventFact> emittedFacts = new ArrayList<>();

        mulliganHarness.setHand(player, List.of(egret, handCard));
        mulliganHarness.setLibrary(player, List.of(topCard, secondCard));

        try (AutoCloseable ignored = mulliganHarness.subscribeToGameEvents(batch ->
                batch.events().forEach(envelope -> emittedFacts.add(envelope.fact())))) {
            mulliganHarness.getGameService().mulligan(gameData, player);
            mulliganHarness.handleMayAbilityChosen(player, true);
        }

        assertThat(gameData.playerHands.get(player.getId())).containsExactly(egret, handCard);
        assertThat(gameData.playerDecks.get(player.getId())).containsExactly(topCard, secondCard);
        assertThat(gameData.mulliganCounts).containsEntry(player.getId(), 0);
        assertThat(gameData.status).isEqualTo(GameStatus.MULLIGAN);
        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
        assertThat(emittedFacts).anyMatch(GameEventFact.PrivateReveal.class::isInstance);
    }

    @Test
    @DisplayName("declining the mulligan action takes a normal mulligan")
    void decliningMulliganActionTakesNormalMulligan() {
        GameTestHarness mulliganHarness = new GameTestHarness();
        Player player = mulliganHarness.getPlayer1();
        GameData gameData = mulliganHarness.getGameData();

        mulliganHarness.setHand(player, List.of(new NoRegretsEgret(), new DarksteelCitadel()));
        mulliganHarness.setLibrary(player, List.of(
                new DarksteelCitadel(), new DarksteelCitadel(), new DarksteelCitadel(),
                new DarksteelCitadel(), new DarksteelCitadel(), new DarksteelCitadel(),
                new DarksteelCitadel()));

        mulliganHarness.getGameService().mulligan(gameData, player);
        mulliganHarness.handleMayAbilityChosen(player, false);

        assertThat(gameData.mulliganCounts).containsEntry(player.getId(), 1);
        assertThat(gameData.playerHands.get(player.getId())).hasSize(7);
        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void lookingAtShortLibraryDoesNotDrawOrMoveCards(int librarySize) {
        GameTestHarness mulliganHarness = new GameTestHarness();
        Player player = mulliganHarness.getPlayer1();
        GameData gameData = mulliganHarness.getGameData();
        NoRegretsEgret egret = new NoRegretsEgret();
        List<DarksteelCitadel> library = librarySize == 0
                ? List.of() : List.of(new DarksteelCitadel());
        mulliganHarness.setHand(player, List.of(egret));
        mulliganHarness.setLibrary(player, library);

        mulliganHarness.getGameService().mulligan(gameData, player);
        mulliganHarness.handleMayAbilityChosen(player, true);

        assertThat(gameData.playerHands.get(player.getId())).containsExactly(egret);
        assertThat(gameData.playerDecks.get(player.getId())).containsExactlyElementsOf(library);
        assertThat(gameData.playersAttemptedDrawFromEmptyLibrary).doesNotContain(player.getId());
        assertThat(gameData.mulliganCounts).containsEntry(player.getId(), 0);
        assertThat(gameData.status).isEqualTo(GameStatus.MULLIGAN);
        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void revealsOnlyEgretPubliclyAndOnlyTopTwoPrivately() throws Exception {
        GameTestHarness mulliganHarness = new GameTestHarness();
        Player player = mulliganHarness.getPlayer1();
        Player opponent = mulliganHarness.getPlayer2();
        GameData gameData = mulliganHarness.getGameData();
        NoRegretsEgret egret = new NoRegretsEgret();
        DarksteelCitadel handCard = new DarksteelCitadel();
        DarksteelCitadel topCard = new DarksteelCitadel();
        DarksteelCitadel secondCard = new DarksteelCitadel();
        DarksteelCitadel thirdCard = new DarksteelCitadel();
        List<GameEventEnvelope> reveals = new ArrayList<>();
        mulliganHarness.setHand(player, List.of(egret, handCard));
        mulliganHarness.setLibrary(player, List.of(topCard, secondCard, thirdCard));

        try (AutoCloseable ignored = mulliganHarness.subscribeToGameEvents(batch ->
                batch.events().stream()
                        .filter(event -> event.fact() instanceof GameEventFact.PrivateReveal)
                        .forEach(reveals::add))) {
            mulliganHarness.getGameService().mulligan(gameData, player);
            mulliganHarness.handleMayAbilityChosen(player, true);
        }

        assertThat(reveals).hasSize(2);
        GameEventFact.PrivateReveal handReveal = (GameEventFact.PrivateReveal) reveals.get(0).fact();
        assertThat(handReveal.zone()).isEqualTo(GameEventFact.RevealZone.HAND);
        assertThat(handReveal.cards()).extracting(GameEventFact.CardSnapshot::cardId)
                .containsExactly(egret.getId());
        assertThat(reveals.get(0).audience().playerIds())
                .containsExactlyInAnyOrder(player.getId(), opponent.getId());
        GameEventFact.PrivateReveal libraryReveal = (GameEventFact.PrivateReveal) reveals.get(1).fact();
        assertThat(libraryReveal.zone()).isEqualTo(GameEventFact.RevealZone.LIBRARY);
        assertThat(libraryReveal.cards()).extracting(GameEventFact.CardSnapshot::cardId)
                .containsExactly(topCard.getId(), secondCard.getId());
        assertThat(reveals.get(1).audience().playerIds()).containsExactly(player.getId());
        assertThat(gameData.playerDecks.get(player.getId()))
                .containsExactly(topCard, secondCard, thirdCard);
    }

    @Test
    void canLookAgainAndThenKeepTheSameHand() {
        GameTestHarness mulliganHarness = new GameTestHarness();
        Player player = mulliganHarness.getPlayer1();
        GameData gameData = mulliganHarness.getGameData();
        NoRegretsEgret egret = new NoRegretsEgret();
        DarksteelCitadel topCard = new DarksteelCitadel();
        DarksteelCitadel secondCard = new DarksteelCitadel();
        mulliganHarness.setHand(player, List.of(egret));
        mulliganHarness.setLibrary(player, List.of(topCard, secondCard));

        mulliganHarness.getGameService().mulligan(gameData, player);
        mulliganHarness.handleMayAbilityChosen(player, true);
        mulliganHarness.getGameService().mulligan(gameData, player);
        mulliganHarness.handleMayAbilityChosen(player, true);
        mulliganHarness.getGameService().keepHand(gameData, player);

        assertThat(gameData.playerHands.get(player.getId())).containsExactly(egret);
        assertThat(gameData.playerDecks.get(player.getId())).containsExactly(topCard, secondCard);
        assertThat(gameData.mulliganCounts).containsEntry(player.getId(), 0);
        assertThat(gameData.playerKeptHand).contains(player.getId());
        assertThat(gameData.playerNeedsToBottom).doesNotContainKey(player.getId());
    }

    @Test
    @CardUsed({NoRegretsEgret.class, DarksteelCitadel.class, SerumPowder.class})
    void decliningSerumPowderStillAllowsEgretBeforeMulligan() {
        GameTestHarness mulliganHarness = new GameTestHarness();
        Player player = mulliganHarness.getPlayer1();
        GameData gameData = mulliganHarness.getGameData();
        SerumPowder powder = new SerumPowder();
        NoRegretsEgret egret = new NoRegretsEgret();
        mulliganHarness.setHand(player, List.of(powder, egret));
        mulliganHarness.setLibrary(player, List.of(
                new DarksteelCitadel(), new DarksteelCitadel(), new DarksteelCitadel(),
                new DarksteelCitadel(), new DarksteelCitadel(), new DarksteelCitadel(),
                new DarksteelCitadel()));

        mulliganHarness.getGameService().mulligan(gameData, player);
        mulliganHarness.handleMayAbilityChosen(player, false);

        assertThat(gameData.mulliganCounts).containsEntry(player.getId(), 0);
        assertThat(gameData.playerHands.get(player.getId())).containsExactly(powder, egret);
        assertThat(gameData.interaction.isAwaitingInput()).isTrue();

        mulliganHarness.handleMayAbilityChosen(player, true);

        assertThat(gameData.playerHands.get(player.getId())).containsExactly(powder, egret);
        assertThat(gameData.mulliganCounts).containsEntry(player.getId(), 0);
    }
}
