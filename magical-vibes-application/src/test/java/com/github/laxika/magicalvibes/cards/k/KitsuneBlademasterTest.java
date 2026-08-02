package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KitsuneBlademasterTest extends BaseCardTest {

    @Test
    @DisplayName("When Kitsune Blademaster becomes blocked, it gets +1/+1 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent blademaster = addReadyBlademaster(player1);
        blademaster.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blademaster.getPowerModifier()).isEqualTo(1);
        assertThat(blademaster.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Kitsune Blademaster blocks, it gets +1/+1 until end of turn")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addReadyBears(player1);
        attacker.setAttacking(true);
        Permanent blademaster = addReadyBlademaster(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blademaster.getPowerModifier()).isEqualTo(1);
        assertThat(blademaster.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Kitsune Blademaster is unblocked, it gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent blademaster = addReadyBlademaster(player1);
        blademaster.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(blademaster.getPowerModifier()).isZero();
        assertThat(blademaster.getToughnessModifier()).isZero();
    }

    private Permanent addReadyBlademaster(Player player) {
        Permanent permanent = new Permanent(new KitsuneBlademaster());
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
