package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.c.CompleteDisregard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.s.StoneHavenMedic;
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

@CardUsed({BloodbondVampire.class, AngelOfMercy.class, GrizzlyBears.class, SoulWarden.class,
        StoneHavenMedic.class, CompleteDisregard.class})
class BloodbondVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Gets one +1/+1 counter when its controller gains life")
    void getsCounterOnLifeGain() {
        Permanent vampire = addCreatureReady(player1, new BloodbondVampire());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers once for each life gain event")
    void triggersOncePerLifeGainEvent() {
        Permanent vampire = addCreatureReady(player1, new BloodbondVampire());
        addCreatureReady(player1, new SoulWarden());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a counter when an opponent gains life")
    void noCounterWhenOpponentGainsLife() {
        Permanent vampire = addCreatureReady(player1, new BloodbondVampire());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Separate life gain events each add a counter in the same turn")
    void separateLifeGainEventsAddSeparateCounters() {
        Permanent vampire = addCreatureReady(player1, new BloodbondVampire());
        addCreatureReady(player1, new StoneHavenMedic());
        addCreatureReady(player1, new StoneHavenMedic());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.activateAbility(player1, 2, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Vampire gets its own counter from the same life gain event")
    void eachVampireGetsItsOwnCounter() {
        Permanent first = addCreatureReady(player1, new BloodbondVampire());
        Permanent second = addCreatureReady(player1, new BloodbondVampire());
        Permanent opposing = addCreatureReady(player2, new BloodbondVampire());
        addCreatureReady(player1, new StoneHavenMedic());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 2, null, null);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter waits for its trigger and is not placed if the Vampire leaves")
    void leavingBeforeTriggerResolvesPreventsCounterPlacement() {
        Permanent vampire = addCreatureReady(player1, new BloodbondVampire());
        addCreatureReady(player1, new StoneHavenMedic());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(new CompleteDisregard()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.castInstant(player2, 0, vampire.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(vampire);
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
