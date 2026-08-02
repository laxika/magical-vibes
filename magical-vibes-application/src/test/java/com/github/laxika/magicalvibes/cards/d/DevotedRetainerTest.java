package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DevotedRetainerTest extends BaseCardTest {

    @Test
    @DisplayName("When Devoted Retainer becomes blocked, it gets +1/+1 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent retainer = addReadyRetainer(player1);
        retainer.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(retainer.getPowerModifier()).isEqualTo(1);
        assertThat(retainer.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Devoted Retainer blocks, it gets +1/+1 until end of turn")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addReadyBears(player1);
        attacker.setAttacking(true);
        Permanent retainer = addReadyRetainer(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(retainer.getPowerModifier()).isEqualTo(1);
        assertThat(retainer.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Devoted Retainer is unblocked, it gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent retainer = addReadyRetainer(player1);
        retainer.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(retainer.getPowerModifier()).isZero();
        assertThat(retainer.getToughnessModifier()).isZero();
    }

    private Permanent addReadyRetainer(Player player) {
        Permanent permanent = new Permanent(new DevotedRetainer());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyBears(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
