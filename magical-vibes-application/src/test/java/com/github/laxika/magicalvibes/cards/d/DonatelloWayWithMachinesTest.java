package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DonatelloWayWithMachines.class, DarksteelRelic.class, Ornithopter.class})
class DonatelloWayWithMachinesTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on Donatello when an artifact enters under its controller's control")
    void addsCounterForControlledArtifactEntry() {
        Permanent donatello = harness.addToBattlefieldAndReturn(player1, new DonatelloWayWithMachines());

        castArtifact(new DarksteelRelic());
        resolveAllTriggers();

        assertThat(donatello.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers once for each controlled artifact that enters")
    void triggersForEachArtifactEntry() {
        Permanent donatello = harness.addToBattlefieldAndReturn(player1, new DonatelloWayWithMachines());

        castArtifact(new DarksteelRelic());
        resolveAllTriggers();
        castArtifact(new Ornithopter());
        resolveAllTriggers();

        assertThat(donatello.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's artifact does not trigger Donatello")
    void opponentArtifactDoesNotTrigger() {
        Permanent donatello = harness.addToBattlefieldAndReturn(player1, new DonatelloWayWithMachines());

        harness.addToBattlefield(player2, new DarksteelRelic());
        resolveAllTriggers();

        assertThat(donatello.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castArtifact(Card artifact) {
        harness.setHand(player1, List.of(artifact));
        harness.castArtifact(player1, 0);
    }
}
