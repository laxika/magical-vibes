package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Flicker;
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

@CardUsed({ImperialCosmographer.class, GrizzlyBears.class, Flicker.class})
class ImperialCosmographerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two +1/+1 counters on itself when another creature you control leaves without dying")
    void putsTwoCountersWhenAllyLeavesWithoutDying() {
        Permanent cosmographer = addCreatureReady(player1, new ImperialCosmographer());
        Permanent leaving = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Flicker()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, leaving.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(cosmographer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when another creature you control dies")
    void doesNotTriggerWhenAllyDies() {
        Permanent cosmographer = addCreatureReady(player1, new ImperialCosmographer());
        Permanent dying = addCreatureReady(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, dying));

        assertThat(cosmographer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
