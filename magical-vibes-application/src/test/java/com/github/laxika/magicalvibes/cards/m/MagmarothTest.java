package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MagmarothTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your upkeep, put a -1/-1 counter on Magmaroth")
    void upkeepPutsMinusCounter() {
        Permanent magmaroth = harness.addToBattlefieldAndReturn(player1, new Magmaroth());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UNTAP -> UPKEEP fires the trigger
        harness.passBothPriorities(); // resolve PutCountersOnSelfEffect

        assertThat(magmaroth.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(magmaroth.getEffectivePower()).isEqualTo(4);
        assertThat(magmaroth.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting a noncreature spell removes a -1/-1 counter")
    void noncreatureSpellRemovesCounter() {
        Permanent magmaroth = harness.addToBattlefieldAndReturn(player1, new Magmaroth());
        magmaroth.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities(); // resolve removal trigger (LIFO on top of Opt)

        assertThat(magmaroth.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not remove a counter")
    void creatureSpellDoesNotRemoveCounter() {
        Permanent magmaroth = harness.addToBattlefieldAndReturn(player1, new Magmaroth());
        magmaroth.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(magmaroth.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }
}
