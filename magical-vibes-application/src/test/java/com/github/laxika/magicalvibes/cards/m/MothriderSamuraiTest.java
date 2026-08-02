package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MothriderSamuraiTest extends BaseCardTest {

    @Test
    @DisplayName("When Mothrider Samurai becomes blocked, it gets +1/+1 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent mothrider = addCreatureReady(player1, new MothriderSamurai());
        mothrider.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mothrider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mothrider)).isEqualTo(3);
    }

    @Test
    @DisplayName("When Mothrider Samurai blocks, it gets +1/+1 until end of turn")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent mothrider = addCreatureReady(player2, new MothriderSamurai());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mothrider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mothrider)).isEqualTo(3);
    }

    @Test
    @DisplayName("When Mothrider Samurai is unblocked, it gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent mothrider = addCreatureReady(player1, new MothriderSamurai());
        mothrider.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gqs.getEffectivePower(gd, mothrider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mothrider)).isEqualTo(2);
    }
}
