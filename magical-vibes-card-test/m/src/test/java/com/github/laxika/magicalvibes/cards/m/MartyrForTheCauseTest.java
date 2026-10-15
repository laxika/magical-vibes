package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MartyrForTheCause.class, GrizzlyBears.class})
class MartyrForTheCauseTest extends BaseCardTest {

    @Test
    @DisplayName("When Martyr for the Cause dies, it proliferates")
    void proliferatesWhenItDies() {
        Permanent martyr = addCreatureReady(player1, new MartyrForTheCause());
        martyr.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent creatureWithCounter = addCreatureReady(player2, new GrizzlyBears());
        creatureWithCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        resolveCombat();
        harness.assertInGraveyard(player1, "Martyr for the Cause");

        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creatureWithCounter.getId()));

        assertThat(creatureWithCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate adds each existing kind to chosen permanents and players only")
    void proliferatesEveryExistingKindOnChosenObjects() {
        Permanent blocker = prepareDeathTrigger();
        blocker.setCounterCount(CounterType.HEXPROOF, 1);
        Permanent unchosen = addCreatureReady(player1, new MartyrForTheCause());
        unchosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent withoutCounters = addCreatureReady(player1, new MartyrForTheCause());
        gd.playerPoisonCounters.put(player2.getId(), 1);
        gd.playerEnergyCounters.put(player2.getId(), 2);
        gd.playerExperienceCounters.put(player1.getId(), 1);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(blocker.getId(), player2.getId()));

        assertThat(blocker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(blocker.getCounterCount(CounterType.HEXPROOF)).isEqualTo(2);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(withoutCounters.getCounters()).isEmpty();
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerExperienceCounters.get(player1.getId())).isEqualTo(1);
        harness.assertInGraveyard(player1, "Martyr for the Cause");
    }

    @Test
    @DisplayName("The death trigger allows choosing no permanents or players")
    void mayChooseNothing() {
        Permanent blocker = prepareDeathTrigger();
        gd.playerPoisonCounters.put(player2.getId(), 1);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(blocker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent prepareDeathTrigger() {
        Permanent martyr = addCreatureReady(player1, new MartyrForTheCause());
        martyr.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MartyrForTheCause());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        return blocker;
    }
}
