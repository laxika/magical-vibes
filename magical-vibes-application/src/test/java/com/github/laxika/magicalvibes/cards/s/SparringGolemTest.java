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

class SparringGolemTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Sparring Golem gets +1/+1 until end of turn")
    void oneBlockerGivesPlusOne() {
        Permanent golem = addReadyGolem(player1);
        golem.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(golem.getPowerModifier()).isEqualTo(1);
        assertThat(golem.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("With three blockers Sparring Golem gets +3/+3 until end of turn")
    void threeBlockersGivesPlusThree() {
        Permanent golem = addReadyGolem(player1);
        golem.setAttacking(true);
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

        assertThat(golem.getPowerModifier()).isEqualTo(3);
        assertThat(golem.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("If unblocked Sparring Golem gets no bonus")
    void unblockedGetsNoBonus() {
        Permanent golem = addReadyGolem(player1);
        golem.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(golem.getPowerModifier()).isZero();
        assertThat(golem.getToughnessModifier()).isZero();
    }

    private Permanent addReadyGolem(Player player) {
        Permanent permanent = new Permanent(new SparringGolem());
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
