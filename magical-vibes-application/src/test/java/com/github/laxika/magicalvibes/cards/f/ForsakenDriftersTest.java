package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Forsaken Drifters")
class ForsakenDriftersTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, its controller mills four cards")
    void deathMillsFourCards() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island()));
        harness.addToBattlefield(player1, new ForsakenDrifters());

        killForsakenDrifters();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof Island).hasSize(4);
    }

    @Test
    @DisplayName("When it dies, it mills its controller's library rather than an opponent's")
    void deathMillsOnlyController() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island()));
        int opponentDeckSize = gd.playerDecks.get(player2.getId()).size();
        int opponentGraveyardSize = gd.playerGraveyards.get(player2.getId()).size();
        harness.addToBattlefield(player1, new ForsakenDrifters());

        killForsakenDrifters();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckSize);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(opponentGraveyardSize);
    }

    @Test
    @DisplayName("When its controller has fewer than four cards, it mills the whole library")
    void deathMillsFewerCardsWhenLibraryIsSmall() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addToBattlefield(player1, new ForsakenDrifters());

        killForsakenDrifters();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof Island).hasSize(2);
    }

    private void killForsakenDrifters() {
        Permanent drifters = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof ForsakenDrifters)
                .findFirst()
                .orElseThrow();
        drifters.setMarkedDamage(4);
        harness.runStateBasedActions();
        resolveAllTriggers();
    }
}
