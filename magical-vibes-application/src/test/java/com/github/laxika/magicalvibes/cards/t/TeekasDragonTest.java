package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TeekasDragonTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Rampage 4 grants no bonus")
    void oneBlockerGivesNothing() {
        Permanent dragon = addReadyDragon(player1);
        dragon.setAttacking(true);
        addReadyHawk(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isZero();
        assertThat(dragon.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("With two blockers Rampage 4 grants +4/+4 until end of turn")
    void twoBlockersGivesPlusFour() {
        Permanent dragon = addReadyDragon(player1);
        dragon.setAttacking(true);
        addReadyHawk(player2);
        addReadyHawk(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(4);
        assertThat(dragon.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("With three blockers Rampage 4 grants +8/+8 until end of turn")
    void threeBlockersGivesPlusEight() {
        Permanent dragon = addReadyDragon(player1);
        dragon.setAttacking(true);
        addReadyHawk(player2);
        addReadyHawk(player2);
        addReadyHawk(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)
        ));
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(8);
        assertThat(dragon.getToughnessModifier()).isEqualTo(8);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent dragon = addReadyDragon(player1);
        dragon.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(dragon.getPowerModifier()).isZero();
        assertThat(dragon.getToughnessModifier()).isZero();
    }

    private Permanent addReadyDragon(Player player) {
        Permanent permanent = new Permanent(new TeekasDragon());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addReadyHawk(Player player) {
        Permanent permanent = new Permanent(new SuntailHawk());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }
}
