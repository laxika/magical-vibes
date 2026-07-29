package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HorribleHordesTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Rampage 1 grants no bonus")
    void oneBlockerGivesNothing() {
        Permanent hordes = addReadyHordes(player1);
        hordes.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(hordes.getPowerModifier()).isZero();
        assertThat(hordes.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("With two blockers Rampage 1 grants +1/+1 until end of turn")
    void twoBlockersGivesPlusOne() {
        Permanent hordes = addReadyHordes(player1);
        hordes.setAttacking(true);
        addReadyBears(player2);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(hordes.getPowerModifier()).isEqualTo(1);
        assertThat(hordes.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("With three blockers Rampage 1 grants +2/+2 until end of turn")
    void threeBlockersGivesPlusTwo() {
        Permanent hordes = addReadyHordes(player1);
        hordes.setAttacking(true);
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

        assertThat(hordes.getPowerModifier()).isEqualTo(2);
        assertThat(hordes.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent hordes = addReadyHordes(player1);
        hordes.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(hordes.getPowerModifier()).isZero();
        assertThat(hordes.getToughnessModifier()).isZero();
    }

    private Permanent addReadyHordes(Player player) {
        Permanent permanent = new Permanent(new HorribleHordes());
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
