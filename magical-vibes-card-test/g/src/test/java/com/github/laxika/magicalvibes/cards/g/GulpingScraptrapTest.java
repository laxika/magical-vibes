package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ContagiousVorrac;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GulpingScraptrap.class, ContagiousVorrac.class})
class GulpingScraptrapTest extends BaseCardTest {

    @Test
    @DisplayName("When Gulping Scraptrap enters, proliferate adds a counter")
    void proliferatesOnEnter() {
        Permanent creatureWithCounter = harness.addToBattlefieldAndReturn(player1, new ContagiousVorrac());
        creatureWithCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new GulpingScraptrap()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creatureWithCounter.getId()));

        assertThat(creatureWithCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Gulping Scraptrap dies, proliferate adds a counter")
    void proliferatesOnDeath() {
        Permanent scraptrap = addCreatureReady(player1, new GulpingScraptrap());
        scraptrap.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GulpingScraptrap());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent creatureWithCounter = harness.addToBattlefieldAndReturn(player2, new ContagiousVorrac());
        creatureWithCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creatureWithCounter.getId()));

        assertThat(creatureWithCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void proliferatesEachCounterKindAndSelectedPlayersOnly() {
        Permanent selected = harness.addToBattlefieldAndReturn(player2, new ContagiousVorrac());
        selected.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        selected.setCounterCount(CounterType.OIL, 3);
        Permanent unselected = harness.addToBattlefieldAndReturn(player1, new ContagiousVorrac());
        unselected.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player1.getId(), 2);
        gd.playerPoisonCounters.put(player2.getId(), 3);

        harness.enterBattlefieldAndReturn(player1, new GulpingScraptrap());
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(selected.getId(), player2.getId()));

        assertThat(selected.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(selected.getCounterCount(CounterType.OIL)).isEqualTo(4);
        assertThat(unselected.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(4);
    }

    @Test
    void mayChooseNothingWhenCountersExist() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new ContagiousVorrac());
        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);

        harness.enterBattlefieldAndReturn(player1, new GulpingScraptrap());
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringWithNoCountersCompletesWithoutAChoice() {
        harness.enterBattlefieldAndReturn(player1, new GulpingScraptrap());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
