package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SamuraiEnforcersTest extends BaseCardTest {

    @Test
    @DisplayName("When Samurai Enforcers becomes blocked, it gets +2/+2 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent samurai = addCreatureReady(player1, new SamuraiEnforcers());
        samurai.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(6);
    }

    @Test
    @DisplayName("When Samurai Enforcers blocks, it gets +2/+2 until end of turn")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent samurai = addCreatureReady(player2, new SamuraiEnforcers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(6);
    }

    @Test
    @DisplayName("When Samurai Enforcers is unblocked, it gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent samurai = addCreatureReady(player1, new SamuraiEnforcers());
        samurai.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(4);
    }
}
