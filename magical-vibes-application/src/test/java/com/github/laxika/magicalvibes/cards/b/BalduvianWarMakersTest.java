package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BalduvianWarMakersTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Rampage 1 grants no bonus")
    void oneBlockerGivesNothing() {
        Permanent warMakers = addReadyWarMakers(player1);
        warMakers.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(warMakers.getPowerModifier()).isZero();
        assertThat(warMakers.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("With three blockers Rampage 1 grants +2/+2 until end of turn")
    void threeBlockersGivesPlusTwo() {
        Permanent warMakers = addReadyWarMakers(player1);
        warMakers.setAttacking(true);
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

        assertThat(warMakers.getPowerModifier()).isEqualTo(2);
        assertThat(warMakers.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent warMakers = addReadyWarMakers(player1);
        warMakers.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(warMakers.getPowerModifier()).isZero();
        assertThat(warMakers.getToughnessModifier()).isZero();
    }

    private Permanent addReadyWarMakers(Player player) {
        Permanent permanent = new Permanent(new BalduvianWarMakers());
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
