package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
