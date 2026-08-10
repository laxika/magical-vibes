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

class RabidWolverinesTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked by one creature gives +1/+1 until end of turn")
    void oneBlockerGivesPlusOne() {
        Permanent wolverines = addWolverinesReady(player1);
        wolverines.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(wolverines.getPowerModifier()).isEqualTo(1);
        assertThat(wolverines.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Becoming blocked by two creatures triggers once for each blocker")
    void twoBlockersGivePlusTwo() {
        Permanent wolverines = addWolverinesReady(player1);
        wolverines.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        resolveAllTriggers();

        assertThat(wolverines.getPowerModifier()).isEqualTo(2);
        assertThat(wolverines.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("An unblocked Rabid Wolverines creates no trigger")
    void unblockedCreatesNoTrigger() {
        Permanent wolverines = addWolverinesReady(player1);
        wolverines.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(wolverines.getPowerModifier()).isZero();
        assertThat(wolverines.getToughnessModifier()).isZero();
    }

    private Permanent addWolverinesReady(Player player) {
        return addCreatureReady(player, new RabidWolverines());
    }
}
