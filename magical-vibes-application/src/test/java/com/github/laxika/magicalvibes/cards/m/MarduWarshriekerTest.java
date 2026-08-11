package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarduWarshriekerTest extends BaseCardTest {

    @Test
    void raidAddsOneManaOfEachMarduColor() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        castMarduWarshrieker();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void raidDoesNotAddManaWithoutAttacking() {
        castMarduWarshrieker();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.assertOnBattlefield(player1, "Mardu Warshrieker");
    }

    @Test
    void raidDoesNotAddManaIfRaidIsLostBeforeResolution() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        castMarduWarshrieker();
        harness.passBothPriorities();
        gd.playersDeclaredAttackersThisTurn.clear();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    private void castMarduWarshrieker() {
        harness.setHand(player1, List.of(new MarduWarshrieker()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
    }
}
