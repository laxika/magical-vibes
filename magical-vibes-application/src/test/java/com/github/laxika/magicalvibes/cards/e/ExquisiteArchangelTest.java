package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExquisiteArchangelTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles itself and resets its controller's life total instead of losing")
    void exilesItselfAndResetsLifeTotal() {
        var angel = harness.addToBattlefieldAndReturn(player1, new ExquisiteArchangel());
        harness.setLibrary(player1, java.util.List.of());
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(angel);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(angel.getCard());
    }

    @Test
    @DisplayName("Can replace a poison loss")
    void replacesPoisonLoss() {
        harness.addToBattlefield(player1, new ExquisiteArchangel());
        harness.setLibrary(player1, java.util.List.of());
        gd.playerPoisonCounters.put(player1.getId(), 10);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Exquisite Archangel"));
    }

    @Test
    @DisplayName("Only replaces one loss")
    void onlyReplacesOneLoss() {
        harness.addToBattlefield(player1, new ExquisiteArchangel());
        harness.setLibrary(player1, java.util.List.of());
        harness.setLife(player1, 0);

        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        harness.setLife(player1, 0);
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
