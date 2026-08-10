package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WurmskinForgerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB can put all three counters on one target creature")
    void distributesAllCountersToOneCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.pendingETBDamageAssignments = Map.of(target.getId(), 3);

        castWurmskinForger();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB distributes three +1/+1 counters among two target creatures")
    void distributesCountersAmongTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        gd.pendingETBDamageAssignments = Map.of(first.getId(), 1, second.getId(), 2);

        castWurmskinForger();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB can distribute one counter to each of three target creatures")
    void distributesCountersAmongThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        gd.pendingETBDamageAssignments = Map.of(
                first.getId(), 1,
                second.getId(), 1,
                third.getId(), 1);

        castWurmskinForger();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB skips an assignment whose permanent is no longer a creature")
    void skipsNoncreatureAssignment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        gd.pendingETBDamageAssignments = Map.of(creature.getId(), 2, artifact.getId(), 1);

        castWurmskinForger();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("ETB skips a target that left the battlefield")
    void skipsTargetThatLeftBattlefield() {
        Permanent staying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent leaving = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.pendingETBDamageAssignments = Map.of(staying.getId(), 2, leaving.getId(), 1);

        castWurmskinForger();
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(leaving.getId()));
        harness.passBothPriorities();

        assertThat(staying.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(leaving.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castWurmskinForger() {
        harness.setHand(player1, List.of(new WurmskinForger()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
    }
}
