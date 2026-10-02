package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodResearcher.class, AngelOfMercy.class})
class BloodResearcherTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when its controller gains life")
    void getsCounterOnLifeGain() {
        Permanent researcher = harness.addToBattlefieldAndReturn(player1, new BloodResearcher());

        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(researcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent gains life")
    void doesNotTriggerOnOpponentLifeGain() {
        Permanent researcher = harness.addToBattlefieldAndReturn(player1, new BloodResearcher());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(researcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Life gain waits for the trigger to resolve and each event adds one counter to each Researcher")
    void separateLifeGainEventsTriggerEachResearcher() {
        harness.setLife(player1, 20);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BloodResearcher());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BloodResearcher());

        for (int event = 0; event < 2; event++) {
            harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.assertLife(player1, 20 + 3 * (event + 1));
            assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(event);
            assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(event);
            assertThat(gd.stack).hasSize(2);

            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(event + 1);
            assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(event + 1);
            assertThat(gd.stack).isEmpty();
        }
    }

    @Test
    @DisplayName("Menace rejects a single blocker")
    void cannotBeBlockedByOneCreature() {
        addCreatureReady(player1, new BloodResearcher());
        addCreatureReady(player2, new BloodResearcher());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace allows two blockers")
    void canBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new BloodResearcher());
        Permanent first = addCreatureReady(player2, new BloodResearcher());
        Permanent second = addCreatureReady(player2, new BloodResearcher());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }
}
