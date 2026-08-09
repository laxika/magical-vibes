package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BarbedSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpinedSliverTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Spined Sliver gets +1/+1 until end of turn")
    void oneBlockerGivesPlusOnePlusOne() {
        Permanent sliver = addReadySliver(player1);
        sliver.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(sliver.getPowerModifier()).isEqualTo(1);
        assertThat(sliver.getToughnessModifier()).isEqualTo(1);
        assertThat(sliver.getEffectivePower()).isEqualTo(3);
        assertThat(sliver.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("With two blockers Spined Sliver gets +2/+2 until end of turn")
    void twoBlockersGivesPlusTwoPlusTwo() {
        Permanent sliver = addReadySliver(player1);
        sliver.setAttacking(true);
        addReadyBears(player2);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(sliver.getPowerModifier()).isEqualTo(2);
        assertThat(sliver.getToughnessModifier()).isEqualTo(2);
        assertThat(sliver.getEffectivePower()).isEqualTo(4);
        assertThat(sliver.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Spined Sliver boosts a Sliver controlled by the other player")
    void boostsOpponentsSliver() {
        Permanent sliver = new Permanent(new BarbedSliver());
        sliver.setSummoningSick(false);
        sliver.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(sliver);
        addReadySliver(player2);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(sliver.getPowerModifier()).isEqualTo(1);
        assertThat(sliver.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Spined Sliver does not trigger for a blocked non-Sliver")
    void doesNotBoostBlockedNonSliver() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        addReadySliver(player2);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("If unblocked Spined Sliver gets no boost")
    void unblockedGetsNoBoost() {
        Permanent sliver = addReadySliver(player1);
        sliver.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(sliver.getPowerModifier()).isZero();
        assertThat(sliver.getToughnessModifier()).isZero();
    }

    private Permanent addReadySliver(Player player) {
        Permanent permanent = new Permanent(new SpinedSliver());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addReadyBears(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }
}
