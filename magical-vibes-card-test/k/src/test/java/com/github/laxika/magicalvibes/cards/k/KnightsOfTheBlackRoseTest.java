package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnightsOfTheBlackRose.class})
class KnightsOfTheBlackRoseTest extends BaseCardTest {

    @Test
    void becomesMonarchWhenItEnters() {
        harness.enterBattlefieldAndReturn(player1, new KnightsOfTheBlackRose());
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void drainsOpponentWhenTheyBecomeMonarchAfterControllerStartedAsMonarch() {
        harness.addToBattlefield(player1, new KnightsOfTheBlackRose());
        gd.monarchPlayerId = player1.getId();
        gd.captureTurnStartSnapshot();

        makeMonarch(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void doesNotDrainWhenControllerWasNotMonarchAtTurnStart() {
        harness.addToBattlefield(player1, new KnightsOfTheBlackRose());
        gd.monarchPlayerId = player2.getId();
        gd.captureTurnStartSnapshot();
        gd.monarchPlayerId = player1.getId();

        makeMonarch(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void doesNotDrainWhenThereWasNoMonarchAtTurnStart() {
        harness.addToBattlefield(player1, new KnightsOfTheBlackRose());
        gd.monarchPlayerId = null;
        gd.captureTurnStartSnapshot();

        makeMonarch(player2);

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void doesNotDrainWhenOpponentIsAlreadyMonarch() {
        harness.addToBattlefield(player1, new KnightsOfTheBlackRose());
        gd.monarchPlayerId = player1.getId();
        gd.captureTurnStartSnapshot();
        makeMonarch(player2);

        makeMonarch(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void drainsEachTimeOpponentRegainsMonarchInTheSameTurn() {
        harness.addToBattlefield(player1, new KnightsOfTheBlackRose());
        gd.monarchPlayerId = player1.getId();
        gd.captureTurnStartSnapshot();
        makeMonarch(player2);
        makeMonarch(player1);
        makeMonarch(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(26);
        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    void drainStillResolvesAfterSourceLeavesAndControllerRegainsMonarch() {
        harness.addToBattlefield(player1, new KnightsOfTheBlackRose());
        gd.monarchPlayerId = player1.getId();
        gd.captureTurnStartSnapshot();
        harness.enterBattlefieldAndReturn(player2, new KnightsOfTheBlackRose());
        harness.passBothPriorities();
        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.enterBattlefieldAndReturn(player1, new KnightsOfTheBlackRose());
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    private void makeMonarch(Player player) {
        harness.enterBattlefieldAndReturn(player, new KnightsOfTheBlackRose());
        resolveAllTriggers();
    }
}
