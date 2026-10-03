package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BondBeetle;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuskLegionDuelist.class, BondBeetle.class, Forest.class, TurnToFrog.class})
class DuskLegionDuelistTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when a +1/+1 counter is put on it")
    void drawsWhenCounterIsPutOnIt() {
        Permanent duelist = addDuelist();
        prepareLibrary(1);
        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        castBondBeetle(duelist);

        assertThat(duelist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .first().isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("Draws only once each turn")
    void drawsOnlyOnceEachTurn() {
        Permanent duelist = addDuelist();
        prepareLibrary(2);
        harness.setHand(player1, List.of(new BondBeetle(), new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        castBondBeetle(duelist);
        castBondBeetle(duelist);

        assertThat(duelist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each Duelist can trigger independently in the same turn")
    void eachDuelistTriggersIndependently() {
        Permanent first = addDuelist();
        Permanent second = addDuelist();
        prepareLibrary(2);
        harness.setHand(player1, List.of(new BondBeetle(), new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        castBondBeetle(first);
        castBondBeetle(second);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counters put on another creature do not trigger the Duelist")
    void doesNotDrawForAnotherCreature() {
        addDuelist();
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BondBeetle());
        prepareLibrary(1);
        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        castBondBeetle(other);

        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed(TurnToFrog.class)
    @DisplayName("Does not draw when it has lost its abilities")
    void doesNotDrawAfterLosingAbilities() {
        Permanent duelist = addDuelist();
        prepareLibrary(1);
        harness.setHand(player1, List.of(new TurnToFrog(), new BondBeetle()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, duelist.getId());
        castBondBeetle(duelist);

        assertThat(duelist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private Permanent addDuelist() {
        return harness.addToBattlefieldAndReturn(player1, new DuskLegionDuelist());
    }

    private void prepareLibrary(int cardCount) {
        harness.setLibrary(player1, java.util.stream.IntStream.range(0, cardCount)
                .mapToObj(i -> new Forest()).toList());
    }

    private void castBondBeetle(Permanent target) {
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
