package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EPFPointSquad.class, GrizzlyBears.class})
class EPFPointSquadTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when another creature you control enters")
    void getsCounterWhenAllyCreatureEnters() {
        Permanent pointSquad = harness.addToBattlefieldAndReturn(player1, new EPFPointSquad());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(pointSquad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature enters")
    void noCounterWhenOpponentCreatureEnters() {
        Permanent pointSquad = harness.addToBattlefieldAndReturn(player1, new EPFPointSquad());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(pointSquad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when Point Squad itself enters")
    void doesNotTriggerForItsOwnEntry() {
        harness.setHand(player1, List.of(new EPFPointSquad()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent pointSquad = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(pointSquad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A second Point Squad triggers the first but not itself")
    void anotherPointSquadTriggersOnlyTheExistingCopy() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EPFPointSquad());
        harness.setHand(player1, List.of(new EPFPointSquad()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent second = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(second).isNotSameAs(first);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Alliance triggers again for each subsequent creature entry")
    void accumulatesCountersFromRepeatedEntries() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EPFPointSquad());

        for (int entry = 0; entry < 2; entry++) {
            harness.setHand(player1, List.of(new EPFPointSquad()));
            harness.addMana(player1, ManaColor.WHITE, 3);
            harness.castCreature(player1, 0);
            resolveAllTriggers();
        }

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1)
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getLast()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
