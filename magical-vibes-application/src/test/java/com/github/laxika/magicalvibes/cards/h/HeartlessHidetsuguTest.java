package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HeartlessHidetsuguTest extends BaseCardTest {

    @Test
    @DisplayName("Deals half each player's life, rounded down")
    void dealsHalfLifeRoundedDown() {
        addCreatureReady(player1, new HeartlessHidetsugu());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Odd life totals round down (21 → 10 damage → 11 life)")
    void oddLifeRoundsDown() {
        addCreatureReady(player1, new HeartlessHidetsugu());
        harness.setLife(player1, 21);
        harness.setLife(player2, 15);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
        assertThat(gd.getLife(player2.getId())).isEqualTo(8);
    }

    @Test
    @DisplayName("Different life totals take different damage")
    void perPlayerAmounts() {
        addCreatureReady(player1, new HeartlessHidetsugu());
        harness.setLife(player1, 10);
        harness.setLife(player2, 40);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(5);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Taps as cost")
    void tapsAsCost() {
        addCreatureReady(player1, new HeartlessHidetsugu());

        harness.activateAbility(player1, 0, null, null);

        assertThat(findPermanent(player1, "Heartless Hidetsugu").isTapped()).isTrue();
    }
}
