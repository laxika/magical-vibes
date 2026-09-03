package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GriffinCanyon;
import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkuDjinn.class, Warthog.class, GriffinCanyon.class})
class AkuDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Your upkeep puts a +1/+1 counter on each creature an opponent controls")
    void upkeepCountersOpponentCreatures() {
        harness.addToBattlefield(player1, new AkuDjinn());
        Permanent oppCreature = harness.addToBattlefieldAndReturn(player2, new Warthog());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new Warthog());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(oppCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not put counters on an opponent's noncreature permanent")
    void doesNotCounterOpponentNoncreatures() {
        harness.addToBattlefield(player1, new AkuDjinn());
        Permanent oppCreature = harness.addToBattlefieldAndReturn(player2, new Warthog());
        Permanent oppLand = harness.addToBattlefieldAndReturn(player2, new GriffinCanyon());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(oppCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(oppLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's upkeep does not put counters")
    void opponentUpkeepDoesNotTrigger() {
        harness.addToBattlefield(player1, new AkuDjinn());
        Permanent oppCreature = harness.addToBattlefieldAndReturn(player2, new Warthog());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(oppCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
