package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SilverstormSamuraiTest extends BaseCardTest {

    @Test
    @DisplayName("When Silverstorm Samurai becomes blocked, it gets +1/+1 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent samurai = addReadySamurai(player1);
        samurai.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(samurai.getPowerModifier()).isEqualTo(1);
        assertThat(samurai.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Silverstorm Samurai blocks, it gets +1/+1 until end of turn")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addReadyBears(player1);
        attacker.setAttacking(true);
        Permanent samurai = addReadySamurai(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(samurai.getPowerModifier()).isEqualTo(1);
        assertThat(samurai.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Silverstorm Samurai is unblocked, it gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent samurai = addReadySamurai(player1);
        samurai.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(samurai.getPowerModifier()).isZero();
        assertThat(samurai.getToughnessModifier()).isZero();
    }

    private Permanent addReadySamurai(Player player) {
        Permanent permanent = new Permanent(new SilverstormSamurai());
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
