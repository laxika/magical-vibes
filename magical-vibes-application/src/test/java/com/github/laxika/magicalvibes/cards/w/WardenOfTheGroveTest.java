package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WardenOfTheGroveTest extends BaseCardTest {

    @Test
    @DisplayName("Warden of the Grove puts a +1/+1 counter on itself at the beginning of its controller's end step")
    void growsAtEndStep() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheGrove());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(warden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Warden of the Grove makes another nontoken creature endure using its counter count")
    void anotherCreatureEnduresWithCounters() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheGrove());
        warden.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent entering = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put 2 +1/+1 counters on this permanent");

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Warden of the Grove can create a Spirit when another nontoken creature endures")
    void anotherCreatureEnduresWithSpirit() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheGrove());
        warden.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a 2/2 white Spirit creature token");

        Permanent spirit = findPermanents(player1, "Spirit").getFirst();
        assertThat(spirit.getCard().getPower()).isEqualTo(2);
        assertThat(spirit.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Warden of the Grove uses its last-known counters if it leaves before endure resolves")
    void usesLastKnownCountersWhenWardenLeaves() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheGrove());
        warden.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent entering = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        warden.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put 2 +1/+1 counters on this permanent");

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
