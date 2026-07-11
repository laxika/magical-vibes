package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IncrementalGrowthTest extends BaseCardTest {

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Distributes 1, 2, and 3 +1/+1 counters across three creatures")
    void distributesCounters() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent b = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent c = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IncrementalGrowth()));
        addMana(player1);

        harness.castSorcery(player1, 0, List.of(a.getId(), b.getId(), c.getId()));
        harness.passBothPriorities();

        assertThat(a.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(b.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(c.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot choose the same creature for two targets")
    void requiresDistinctTargets() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent b = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new IncrementalGrowth()));
        addMana(player1);

        UUID aId = a.getId();
        UUID bId = b.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(aId, bId, aId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-creature permanent is an illegal target")
    void rejectsNonCreatureTarget() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent b = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new com.github.laxika.magicalvibes.cards.f.Forest());
        harness.setHand(player1, List.of(new IncrementalGrowth()));
        addMana(player1);

        UUID aId = a.getId();
        UUID bId = b.getId();
        UUID landId = land.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(aId, bId, landId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
