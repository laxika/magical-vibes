package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CoralhelmGuide;
import com.github.laxika.magicalvibes.cards.s.SludgeCrawler;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DominatorDrone.class, CoralhelmGuide.class, SludgeCrawler.class})
class DominatorDroneTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes each opponent lose 2 life when you control another colorless creature")
    void etbWithAnotherColorlessCreature() {
        harness.addToBattlefield(player1, new SludgeCrawler());
        castDominatorDrone();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB does not trigger without another colorless creature")
    void etbWithoutAnotherColorlessCreature() {
        harness.addToBattlefield(player1, new CoralhelmGuide());
        castDominatorDrone();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Ingest exiles the top card of the damaged player's library")
    void ingestExilesTopCard() {
        Permanent drone = addCreatureReady(player1, new DominatorDrone());
        drone.setAttacking(true);
        CoralhelmGuide topCard = new CoralhelmGuide();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();
        harness.passBothPriorities();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
    }

    @Test
    @DisplayName("Dominator Drone does not count itself as another colorless creature")
    void etbAloneDoesNotTrigger() {
        castDominatorDrone();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent's colorless creature does not satisfy the ETB condition")
    void opponentsColorlessCreatureDoesNotCount() {
        harness.addToBattlefield(player2, new SludgeCrawler());
        castDominatorDrone();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB does nothing if the last other colorless creature leaves before resolution")
    void etbRechecksConditionOnResolution() {
        Permanent crawler = harness.addToBattlefieldAndReturn(player1, new SludgeCrawler());
        castDominatorDrone();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(crawler);
        harness.setGraveyard(player1, List.of(crawler.getCard()));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB still resolves after Dominator Drone leaves if another colorless creature remains")
    void etbResolvesWithoutSource() {
        harness.addToBattlefield(player1, new SludgeCrawler());
        castDominatorDrone();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        Permanent drone = findPermanent(player1, "Dominator Drone");
        gd.playerBattlefields.get(player1.getId()).remove(drone);
        harness.setGraveyard(player1, List.of(drone.getCard()));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Ingest resolves harmlessly when the damaged player has an empty library")
    void ingestWithEmptyLibrary() {
        Permanent drone = addCreatureReady(player1, new DominatorDrone());
        drone.setAttacking(true);
        harness.setLibrary(player2, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Ingest exiles exactly one card from the damaged player's library")
    void ingestExilesOnlyOpponentsTopCard() {
        Permanent drone = addCreatureReady(player1, new DominatorDrone());
        drone.setAttacking(true);
        CoralhelmGuide ownTopCard = new CoralhelmGuide();
        CoralhelmGuide opponentTopCard = new CoralhelmGuide();
        SludgeCrawler opponentSecondCard = new SludgeCrawler();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of(opponentTopCard, opponentSecondCard));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.findExiledCard(opponentTopCard.getId())).isNotNull();
        assertThat(gd.findExiledCard(opponentSecondCard.getId())).isNull();
        assertThat(gd.findExiledCard(ownTopCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTopCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentSecondCard);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    private void castDominatorDrone() {
        harness.castFromHand(player1, new DominatorDrone(), "{2}{B}");
    }
}
