package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GorillaBerserkersTest extends BaseCardTest {

    @Test
    @DisplayName("Can't be blocked by fewer than three creatures")
    void cannotBeBlockedByTwo() {
        addAttackingBerserkers();
        addBlockers(3);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("3 or more creatures");
    }

    @Test
    @DisplayName("With three blockers Rampage 2 grants +4/+4 until end of turn")
    void threeBlockersGivesPlusFour() {
        Permanent berserkers = addAttackingBerserkers();
        addBlockers(3);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));
        harness.passBothPriorities();

        assertThat(berserkers.getPowerModifier()).isEqualTo(4);
        assertThat(berserkers.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("With four blockers Rampage 2 grants +6/+6 until end of turn")
    void fourBlockersGivesPlusSix() {
        Permanent berserkers = addAttackingBerserkers();
        addBlockers(4);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0),
                new BlockerAssignment(3, 0)));
        harness.passBothPriorities();

        assertThat(berserkers.getPowerModifier()).isEqualTo(6);
        assertThat(berserkers.getToughnessModifier()).isEqualTo(6);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent berserkers = addAttackingBerserkers();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(berserkers.getPowerModifier()).isZero();
    }

    private Permanent addAttackingBerserkers() {
        Permanent permanent = new Permanent(new GorillaBerserkers());
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private void addBlockers(int count) {
        for (int i = 0; i < count; i++) {
            Permanent blocker = new Permanent(new GrizzlyBears());
            blocker.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(blocker);
        }
    }
}
