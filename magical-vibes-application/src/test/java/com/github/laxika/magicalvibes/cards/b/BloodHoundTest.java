package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodHoundTest extends BaseCardTest {

    @Test
    @DisplayName("Damage to its controller may put that much +1/+1 counters on Blood Hound")
    void damageAddsThatManyCountersWhenAccepted() {
        Permanent hound = harness.addToBattlefieldAndReturn(player1, new BloodHound());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities(); // Lightning Bolt resolves.
        harness.passBothPriorities(); // Resolve Blood Hound's trigger.
        harness.handleMayAbilityChosen(player1, true);

        assertThat(hound.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Declining the damage trigger does not add counters")
    void damageDoesNotAddCountersWhenDeclined() {
        Permanent hound = harness.addToBattlefieldAndReturn(player1, new BloodHound());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(hound.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("All +1/+1 counters are removed at its controller's end step")
    void removesCountersAtEndStep() {
        Permanent hound = harness.addToBattlefieldAndReturn(player1, new BloodHound());
        hound.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hound.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
