package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerdurousGearhulkTest extends BaseCardTest {

    @Test
    @DisplayName("ETB distributes four +1/+1 counters among your creatures")
    void distributesCountersAmongControlledCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Map<UUID, Integer> assignments = Map.of(first.getId(), 1, second.getId(), 3);
        gd.pendingETBDamageAssignments = assignments;

        castGearhulk(assignments);

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Map<UUID, Integer> assignments = Map.of(opponentCreature.getId(), 4);
        gd.pendingETBDamageAssignments = assignments;

        assertThatThrownBy(() -> castGearhulk(assignments))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters assigned to a creature that leaves are lost")
    void losesCountersAssignedToCreatureThatLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Map<UUID, Integer> assignments = Map.of(target.getId(), 4);
        gd.pendingETBDamageAssignments = assignments;

        castGearhulk(assignments);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castGearhulk(Map<UUID, Integer> assignments) {
        harness.setHand(player1, List.of(new VerdurousGearhulk()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, assignments);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
