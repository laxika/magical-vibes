package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SkywarpSkaab;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ApprenticeSharpshooter.class, GrizzlyBears.class, SkywarpSkaab.class})
class ApprenticeSharpshooterTest extends BaseCardTest {

    @Test
    @DisplayName("Training puts a +1/+1 counter on Apprentice Sharpshooter when it attacks with a greater-power creature")
    void trainingTriggersWithGreaterPowerAlly() {
        Permanent sharpshooter = addCreatureReady(player1, new ApprenticeSharpshooter());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        ally.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(0, 1));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(sharpshooter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Training does not trigger when Apprentice Sharpshooter attacks alone")
    void trainingDoesNotTriggerAlone() {
        addCreatureReady(player1, new ApprenticeSharpshooter());

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Training does not trigger with a creature that does not have greater power")
    void trainingDoesNotTriggerWithoutGreaterPowerAlly() {
        Permanent sharpshooter = addCreatureReady(player1, new ApprenticeSharpshooter());
        sharpshooter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        assertThat(gd.stack).isEmpty();
    }
    @Test
    void reachAllowsBlockingFlyingCreature() {
        Permanent sharpshooter = addCreatureReady(player2, new ApprenticeSharpshooter());
        addCreatureReady(player1, new SkywarpSkaab());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(sharpshooter.isBlocking()).isTrue();
    }

    @Test
    void trainingTriggersOnlyOnceWithMultipleGreaterPowerAttackers() {
        Permanent sharpshooter = addCreatureReady(player1, new ApprenticeSharpshooter());
        addCreatureReady(player1, new SkywarpSkaab());
        addCreatureReady(player1, new SkywarpSkaab());

        declareAttackers(List.of(0, 1, 2));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(sharpshooter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void trainingStillResolvesAfterAllyLosesGreaterPower() {
        Permanent sharpshooter = addCreatureReady(player1, new ApprenticeSharpshooter());
        Permanent ally = addCreatureReady(player1, new ApprenticeSharpshooter());
        ally.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(0, 1));

        assertThat(gd.stack).hasSize(1);
        ally.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        resolveAllTriggers();
        assertThat(sharpshooter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void trainingDoesNotTriggerWithNonattackingGreaterPowerCreature() {
        Permanent sharpshooter = addCreatureReady(player1, new ApprenticeSharpshooter());
        addCreatureReady(player1, new SkywarpSkaab());

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(sharpshooter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
