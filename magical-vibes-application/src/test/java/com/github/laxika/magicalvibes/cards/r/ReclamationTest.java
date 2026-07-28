package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZombieGoliath;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReclamationTest extends BaseCardTest {

    @Test
    @DisplayName("Black creature can't attack when its controller controls no land")
    void blackCreatureCannotAttackWithoutLand() {
        harness.addToBattlefield(player2, new Reclamation());
        addCreatureReady(player1, new ZombieGoliath());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attacking with a black creature sacrifices one land")
    void attackingBlackCreatureSacrificesOneLand() {
        harness.addToBattlefield(player2, new Reclamation());
        addCreatureReady(player1, new ZombieGoliath());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player1, List.of(0));

        assertThat(forestCount(player1)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(lifeBefore);
    }

    @Test
    @DisplayName("Two black attackers need two lands — one land isn't enough")
    void twoBlackAttackersNeedTwoLands() {
        harness.addToBattlefield(player2, new Reclamation());
        addCreatureReady(player1, new ZombieGoliath());
        addCreatureReady(player1, new ZombieGoliath());
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(forestCount(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two black attackers sacrifice two lands")
    void twoBlackAttackersSacrificeTwoLands() {
        harness.addToBattlefield(player2, new Reclamation());
        addCreatureReady(player1, new ZombieGoliath());
        addCreatureReady(player1, new ZombieGoliath());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0, 1));

        assertThat(forestCount(player1)).isZero();
    }

    @Test
    @DisplayName("Non-black creatures attack freely and sacrifice nothing")
    void nonBlackCreatureAttacksWithoutSacrifice() {
        harness.addToBattlefield(player2, new Reclamation());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0));

        assertThat(forestCount(player1)).isEqualTo(1);
    }

    private int forestCount(Player player) {
        return (int) countPermanents(player, "Forest");
    }
}
