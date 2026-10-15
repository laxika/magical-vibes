package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BellowingSaddlebrute.class})
class BellowingSaddlebruteTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes you lose 4 life when you did not attack this turn")
    void losesLifeWithoutRaid() {
        int lifeBefore = gd.getLife(player1.getId());

        castBellowingSaddlebrute();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("ETB does not make you lose life when raid is met")
    void doesNotLoseLifeWithRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        int lifeBefore = gd.getLife(player1.getId());

        castBellowingSaddlebrute();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("ETB checks raid when its trigger resolves")
    void checksRaidAtResolution() {
        int lifeBefore = gd.getLife(player1.getId());

        castBellowingSaddlebrute();
        harness.passBothPriorities();
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("An opponent's attack does not satisfy raid")
    void opponentsAttackDoesNotPreventLifeLoss() {
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        castBellowingSaddlebrute();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore - 4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("The enter ability still triggers when raid is met")
    void triggersEvenWithRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        castBellowingSaddlebrute();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    private void castBellowingSaddlebrute() {
        harness.castFromHand(player1, new BellowingSaddlebrute(), "{3}{B}");
    }
}
