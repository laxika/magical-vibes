package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HuntedWitness;
import com.github.laxika.magicalvibes.cards.j.JusticeStrike;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeneratedLoxodon.class, GrizzlyBears.class, HuntedWitness.class, JusticeStrike.class})
class VeneratedLoxodonTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each creature that convoked it")
    void putsCountersOnConvokeCreatures() {
        Permanent firstConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent thirdConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent nonConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VeneratedLoxodon()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(firstConvokeCreature.getId(), secondConvokeCreature.getId(), thirdConvokeCreature.getId()));
        resolveAllTriggers();

        assertThat(firstConvokeCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondConvokeCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(thirdConvokeCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonConvokeCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting without convoke puts no counters on any creature")
    void noCountersWithoutConvoke() {
        harness.addToBattlefield(player1, new HuntedWitness());
        harness.setHand(player1, List.of(new VeneratedLoxodon()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Venerated Loxodon");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allSatisfy(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    @DisplayName("Five newly entered white creatures pay the entire cost and get counters only when the enter trigger resolves")
    void fullConvokeWithSummoningSickCreatures() {
        List<Permanent> convokers = java.util.stream.IntStream.range(0, 5)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new HuntedWitness()))
                .toList();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HuntedWitness());
        harness.setHand(player1, List.of(new VeneratedLoxodon()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                convokers.stream().map(Permanent::getId).toList());

        assertThat(convokers).allSatisfy(permanent -> {
            assertThat(permanent.isTapped()).isTrue();
            assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        });
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Venerated Loxodon");
        assertThat(convokers).allSatisfy(permanent ->
                assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());

        resolveAllTriggers();

        assertThat(convokers).allSatisfy(permanent ->
                assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> !convokers.contains(permanent))
                .allSatisfy(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    @DisplayName("A convoker that dies before the enter trigger resolves is skipped")
    void missingConvokerDoesNotPreventOtherCounters() {
        Permanent survivingConvoker = harness.addToBattlefieldAndReturn(player1, new HuntedWitness());
        Permanent dyingConvoker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VeneratedLoxodon()));
        harness.setHand(player2, List.of(new JusticeStrike()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(survivingConvoker.getId(), dyingConvoker.getId()));
        harness.passBothPriorities();
        harness.castInstant(player2, 0, dyingConvoker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(survivingConvoker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        resolveAllTriggers();

        assertThat(survivingConvoker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(dyingConvoker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
