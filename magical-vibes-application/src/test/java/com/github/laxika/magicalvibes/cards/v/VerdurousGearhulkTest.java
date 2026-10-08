package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VerdurousGearhulk.class, GrizzlyBears.class})
class VerdurousGearhulkTest extends BaseCardTest {

    @Test
    @DisplayName("ETB distributes four +1/+1 counters among your creatures")
    void distributesCountersAmongControlledCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Map<UUID, Integer> assignments = Map.of(first.getId(), 1, second.getId(), 3);
        gd.pendingETBDamageAssignments = assignments;

        castGearhulk();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Map<UUID, Integer> assignments = Map.of(opponentCreature.getId(), 4);
        gd.pendingETBDamageAssignments = assignments;

        castGearhulk();
        harness.passBothPriorities();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counters assigned to a creature that leaves are lost")
    void losesCountersAssignedToCreatureThatLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Map<UUID, Integer> assignments = Map.of(target.getId(), 4);
        gd.pendingETBDamageAssignments = assignments;

        castGearhulk();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gearhulk can assign all four counters to itself")
    void canTargetItself() {
        castGearhulk();
        harness.passBothPriorities();
        UUID gearhulkId = harness.getPermanentId(player1, "Verdurous Gearhulk");
        harness.handlePermanentChosen(player1, gearhulkId);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handleListChoice(player1, "4");
        harness.passBothPriorities();

        Permanent gearhulk = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(gearhulkId))
                .findFirst().orElseThrow();
        assertThat(gearhulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counters on a surviving target are not increased when another target leaves")
    void doesNotRedistributeCountersFromMissingTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castGearhulk();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handleListChoice(player1, "1");
        harness.handleListChoice(player1, "3");
        gd.playerBattlefields.get(player1.getId()).remove(second);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castGearhulk() {
        harness.castFromHand(player1, new VerdurousGearhulk(), "{3}{G}{G}");
    }

    @Test
    @DisplayName("The controller can choose no targets")
    void canChooseNoTargets() {
        castGearhulk();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allSatisfy(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isZero());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A target controlled by an opponent at resolution receives no counters")
    void targetChangingControllerReceivesNoCounters() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castGearhulk();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handleListChoice(player1, "1");
        harness.handleListChoice(player1, "3");
        gd.playerBattlefields.get(player1.getId()).remove(second);
        gd.playerBattlefields.get(player2.getId()).add(second);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
