package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CrashThrough;
import com.github.laxika.magicalvibes.cards.f.FrilledSandwalla;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Magmaroth.class, CrashThrough.class, FrilledSandwalla.class})
class MagmarothTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your upkeep, put a -1/-1 counter on Magmaroth")
    void upkeepPutsMinusCounter() {
        Permanent magmaroth = harness.addToBattlefieldAndReturn(player1, new Magmaroth());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(magmaroth.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(magmaroth.getEffectivePower()).isEqualTo(4);
        assertThat(magmaroth.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting a noncreature spell removes a -1/-1 counter")
    void noncreatureSpellRemovesCounter() {
        Permanent magmaroth = harness.addToBattlefieldAndReturn(player1, new Magmaroth());
        magmaroth.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.setHand(player1, List.of(new CrashThrough()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities(); // Resolve the removal trigger before the spell.

        assertThat(magmaroth.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not remove a counter")
    void creatureSpellDoesNotRemoveCounter() {
        Permanent magmaroth = harness.addToBattlefieldAndReturn(player1, new Magmaroth());
        magmaroth.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.setHand(player1, List.of(new FrilledSandwalla()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(magmaroth.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    void opponentsUpkeepDoesNotPutCounter() {
        Permanent magmaroth = harness.addToBattlefieldAndReturn(player1, new Magmaroth());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(magmaroth.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void opponentsNoncreatureSpellDoesNotRemoveCounter() {
        Permanent magmaroth = harness.addToBattlefieldAndReturn(player1, new Magmaroth());
        magmaroth.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new CrashThrough()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorcery(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(magmaroth.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    void noncreatureSpellTriggersEvenWithoutCounters() {
        Permanent magmaroth = harness.addToBattlefieldAndReturn(player1, new Magmaroth());
        harness.setHand(player1, List.of(new CrashThrough()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();

        assertThat(magmaroth.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void eachMagmarothRemovesOnlyItsOwnCounter() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Magmaroth());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Magmaroth());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new Magmaroth());
        first.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        second.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        opponents.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.setHand(player1, List.of(new CrashThrough()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0);
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(opponents.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
    }
}
