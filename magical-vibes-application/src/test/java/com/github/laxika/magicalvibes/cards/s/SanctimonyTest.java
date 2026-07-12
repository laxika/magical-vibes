package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SanctimonyTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent tapping a Mountain for mana gains the controller 1 life")
    void opponentTapsMountainGainsLife() {
        harness.addToBattlefield(player1, new Sanctimony());
        harness.addToBattlefield(player2, new Mountain());
        harness.setLife(player1, 20);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Controller tapping their own Mountain does not gain life (only opponents)")
    void controllerTapsMountainNoLife() {
        harness.addToBattlefield(player1, new Sanctimony());
        harness.addToBattlefield(player1, new Mountain());
        harness.setLife(player1, 20);

        // Mountain is at index 1 (Sanctimony at index 0)
        harness.tapPermanent(player1, 1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Opponent tapping a non-Mountain land does not gain life")
    void opponentTapsNonMountainNoLife() {
        harness.addToBattlefield(player1, new Sanctimony());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player1, 20);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each opponent Mountain tap triggers Sanctimony separately")
    void multipleMountainTapsGainMultipleLife() {
        harness.addToBattlefield(player1, new Sanctimony());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.setLife(player1, 20);

        harness.tapPermanent(player2, 0);
        harness.tapPermanent(player2, 1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }
}
