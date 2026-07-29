package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JungleWurmTest extends BaseCardTest {

    @Test
    @DisplayName("With a single blocker Jungle Wurm is unchanged")
    void oneBlockerGivesNoPenalty() {
        Permanent wurm = addReadyWurm(player1);
        wurm.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(wurm.getPowerModifier()).isZero();
        assertThat(wurm.getToughnessModifier()).isZero();
        assertThat(wurm.getEffectivePower()).isEqualTo(5);
        assertThat(wurm.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("With three blockers Jungle Wurm gets -2/-2 until end of turn")
    void threeBlockersGiveMinusTwo() {
        Permanent wurm = addReadyWurm(player1);
        wurm.setAttacking(true);
        addReadyBears(player2);
        addReadyBears(player2);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)
        ));
        harness.passBothPriorities();

        assertThat(wurm.getPowerModifier()).isEqualTo(-2);
        assertThat(wurm.getToughnessModifier()).isEqualTo(-2);
        assertThat(wurm.getEffectivePower()).isEqualTo(3);
        assertThat(wurm.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("If unblocked Jungle Wurm is unchanged")
    void unblockedGivesNoPenalty() {
        Permanent wurm = addReadyWurm(player1);
        wurm.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(wurm.getPowerModifier()).isZero();
        assertThat(wurm.getToughnessModifier()).isZero();
    }

    private Permanent addReadyWurm(Player player) {
        Permanent permanent = new Permanent(new JungleWurm());
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
