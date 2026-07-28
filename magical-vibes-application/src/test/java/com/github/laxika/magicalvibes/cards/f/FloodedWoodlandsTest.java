package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FloodedWoodlandsTest extends BaseCardTest {

    @Test
    @DisplayName("Green creature can't attack when its controller controls no land")
    void greenCreatureCannotAttackWithoutLand() {
        harness.addToBattlefield(player2, new FloodedWoodlands());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attacking with a green creature sacrifices one land")
    void attackingGreenCreatureSacrificesOneLand() {
        harness.addToBattlefield(player2, new FloodedWoodlands());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player1, List.of(0));

        assertThat(forestCount(player1)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(lifeBefore);
    }

    @Test
    @DisplayName("Two green attackers need two lands — one land isn't enough")
    void twoGreenAttackersNeedTwoLands() {
        harness.addToBattlefield(player2, new FloodedWoodlands());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(forestCount(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two green attackers sacrifice two lands")
    void twoGreenAttackersSacrificeTwoLands() {
        harness.addToBattlefield(player2, new FloodedWoodlands());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0, 1));

        assertThat(forestCount(player1)).isZero();
    }

    @Test
    @DisplayName("Non-green creatures attack freely and sacrifice nothing")
    void nonGreenCreatureAttacksWithoutSacrifice() {
        harness.addToBattlefield(player2, new FloodedWoodlands());
        addCreatureReady(player1, new HillGiant());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0));

        assertThat(forestCount(player1)).isEqualTo(1);
    }

    private int forestCount(Player player) {
        return (int) countPermanents(player, "Forest");
    }
}
