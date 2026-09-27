package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BileUrchin.class)
class BileUrchinTest extends BaseCardTest {

    private static final int STARTING_LIFE = 20;

    private void addReadyBileUrchin() {
        addCreatureReady(player1, new BileUrchin());
    }

    @Test
    @DisplayName("Sacrificing Bile Urchin makes the targeted opponent lose 1 life")
    void opponentLosesOneLife() {
        addReadyBileUrchin();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, STARTING_LIFE - 1);
        harness.assertLife(player1, STARTING_LIFE);
        harness.assertInGraveyard(player1, "Bile Urchin");
    }

    @Test
    @DisplayName("Bile Urchin can target its own controller")
    void canTargetController() {
        addReadyBileUrchin();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, STARTING_LIFE - 1);
        harness.assertLife(player2, STARTING_LIFE);
    }

    @Test
    @DisplayName("Sacrifice cost is paid immediately, before the ability resolves")
    void sacrificeHappensOnActivation() {
        addReadyBileUrchin();

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Bile Urchin");
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, STARTING_LIFE);
    }

    @Test
    @DisplayName("Summoning sick Bile Urchin can still be sacrificed (no tap in the cost)")
    void worksWhileSummoningSick() {
        harness.addToBattlefield(player1, new BileUrchin());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, STARTING_LIFE - 1);
    }
}
