package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BeastWithin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CanopyGargantuan.class, GrizzlyBears.class, BeastWithin.class})
class CanopyGargantuanTest extends BaseCardTest {

    @Test
    @CardUsed({CanopyGargantuan.class, BeastWithin.class})
    @DisplayName("Ward counters an opponent's spell when they cannot pay two mana")
    void wardCountersSpellWhenOpponentCannotPay() {
        Permanent gargantuan = addCreatureReady(player1, new CanopyGargantuan());
        harness.setHand(player2, List.of(new BeastWithin()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castInstant(player2, 0, gargantuan.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(gargantuan);
        harness.assertInGraveyard(player2, "Beast Within");
        assertThat(countPermanents(player1, "Beast")).isZero();
    }

    @Test
    @DisplayName("At upkeep, puts counters equal to each other creature's toughness")
    void putsCountersEqualToEachOtherCreaturesToughness() {
        Permanent gargantuan = addCreatureReady(player1, new CanopyGargantuan());
        Permanent twoTwo = addCreatureReady(player1, new GrizzlyBears());
        Permanent threeThree = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        threeThree.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gargantuan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(twoTwo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(threeThree.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @CardUsed({CanopyGargantuan.class})
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent first = addCreatureReady(player1, new CanopyGargantuan());
        Permanent second = addCreatureReady(player1, new CanopyGargantuan());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Multiple Gargantuans grow each other and recalculate toughness for each trigger")
    void multipleGargantuansRecalculateToughnessForEachTrigger() {
        Permanent first = addCreatureReady(player1, new CanopyGargantuan());
        Permanent second = addCreatureReady(player1, new CanopyGargantuan());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("A trigger survives its source leaving and uses toughness at resolution")
    void triggerSurvivesSourceLeavingAndUsesCurrentToughness() {
        Permanent gargantuan = addCreatureReady(player1, new CanopyGargantuan());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(gargantuan);
        gd.playerGraveyards.get(player1.getId()).add(gargantuan.getCard());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }
}
