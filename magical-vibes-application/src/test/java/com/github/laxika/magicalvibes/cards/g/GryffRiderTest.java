package com.github.laxika.magicalvibes.cards.g;

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

@CardUsed({GryffRider.class, GrizzlyBears.class})
class GryffRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Training puts a +1/+1 counter on Gryff Rider when it attacks with a greater-power creature")
    void trainingTriggersWithGreaterPowerAlly() {
        Permanent rider = addCreatureReady(player1, new GryffRider());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        ally.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(0, 1));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(rider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Training does not trigger when Gryff Rider attacks alone")
    void trainingDoesNotTriggerAlone() {
        addCreatureReady(player1, new GryffRider());

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Training does not trigger with a creature that does not have greater power")
    void trainingDoesNotTriggerWithoutGreaterPowerAlly() {
        Permanent rider = addCreatureReady(player1, new GryffRider());
        rider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        assertThat(gd.stack).isEmpty();
    }
}
