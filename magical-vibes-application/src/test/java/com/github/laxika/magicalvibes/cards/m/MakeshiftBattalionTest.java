package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MakeshiftBattalion.class, AlpineWatchdog.class, Shock.class})
class MakeshiftBattalionTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion puts a +1/+1 counter on Makeshift Battalion")
    void battalionPutsCounterOnSource() {
        Permanent battalion = addCreatureReady(player1, new MakeshiftBattalion());
        addCreatureReady(player1, new AlpineWatchdog());
        addCreatureReady(player1, new AlpineWatchdog());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(battalion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Battalion does not trigger with only one other attacker")
    void battalionDoesNotTriggerWithTooFewAttackers() {
        Permanent battalion = addCreatureReady(player1, new MakeshiftBattalion());
        addCreatureReady(player1, new AlpineWatchdog());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(battalion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Battalion must itself attack even when three allies attack")
    void nonattackingBattalionDoesNotGetCounter() {
        Permanent battalion = addCreatureReady(player1, new MakeshiftBattalion());
        addCreatureReady(player1, new AlpineWatchdog());
        addCreatureReady(player1, new AlpineWatchdog());
        addCreatureReady(player1, new AlpineWatchdog());

        declareAttackers(player1, List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(battalion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each attacking Battalion gets exactly one counter with more than three attackers")
    void multipleBattalionsEachGetOneCounter() {
        Permanent first = addCreatureReady(player1, new MakeshiftBattalion());
        Permanent second = addCreatureReady(player1, new MakeshiftBattalion());
        Permanent third = addCreatureReady(player1, new AlpineWatchdog());
        Permanent fourth = addCreatureReady(player1, new AlpineWatchdog());

        declareAttackers(player1, List.of(0, 1, 2, 3));
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(fourth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Battalion still gets its counter after another attacker dies in response")
    void attackerCountIsNotRecheckedAtResolution() {
        Permanent battalion = addCreatureReady(player1, new MakeshiftBattalion());
        Permanent ally = addCreatureReady(player1, new AlpineWatchdog());
        addCreatureReady(player1, new AlpineWatchdog());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        declareAttackers(player1, List.of(0, 1, 2));
        assertThat(gd.stack).hasSize(1);
        assertThat(battalion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, ally.getId());
        harness.assertInGraveyard(player1, "Alpine Watchdog");
        resolveAllTriggers();

        assertThat(battalion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
