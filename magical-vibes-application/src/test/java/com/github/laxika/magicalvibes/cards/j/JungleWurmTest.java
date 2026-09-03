package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JungleWurm.class, GiantMantis.class})
class JungleWurmTest extends BaseCardTest {

    @Test
    @DisplayName("With a single blocker Jungle Wurm is unchanged")
    void oneBlockerGivesNoPenalty() {
        Permanent wurm = addCreatureReady(player1, new JungleWurm());
        wurm.setAttacking(true);
        addCreatureReady(player2, new GiantMantis());

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
        Permanent wurm = addCreatureReady(player1, new JungleWurm());
        wurm.setAttacking(true);
        addCreatureReady(player2, new GiantMantis());
        addCreatureReady(player2, new GiantMantis());
        addCreatureReady(player2, new GiantMantis());

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
        Permanent wurm = addCreatureReady(player1, new JungleWurm());
        wurm.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(wurm.getPowerModifier()).isZero();
        assertThat(wurm.getToughnessModifier()).isZero();
    }
}
