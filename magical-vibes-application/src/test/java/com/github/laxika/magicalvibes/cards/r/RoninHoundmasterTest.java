package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoninHoundmasterTest extends BaseCardTest {

    @Test
    @DisplayName("When Ronin Houndmaster becomes blocked, it gets +1/+1 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent houndmaster = addReadyHoundmaster(player1);
        houndmaster.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(houndmaster.getPowerModifier()).isEqualTo(1);
        assertThat(houndmaster.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Ronin Houndmaster blocks, it gets +1/+1 until end of turn")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addReadyBears(player1);
        attacker.setAttacking(true);
        Permanent houndmaster = addReadyHoundmaster(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(houndmaster.getPowerModifier()).isEqualTo(1);
        assertThat(houndmaster.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Ronin Houndmaster is unblocked, it gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent houndmaster = addReadyHoundmaster(player1);
        houndmaster.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(houndmaster.getPowerModifier()).isZero();
        assertThat(houndmaster.getToughnessModifier()).isZero();
    }

    private Permanent addReadyHoundmaster(Player player) {
        Permanent permanent = new Permanent(new RoninHoundmaster());
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
