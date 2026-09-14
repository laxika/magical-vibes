package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.Sizzle;
import com.github.laxika.magicalvibes.cards.s.ShockTroops;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodHound.class, Sizzle.class, ShockTroops.class})
class BloodHoundTest extends BaseCardTest {

    @Test
    @DisplayName("Damage to its controller may put that much +1/+1 counters on Blood Hound")
    void damageAddsThatManyCountersWhenAccepted() {
        Permanent hound = harness.addToBattlefieldAndReturn(player2, new BloodHound());
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new Sizzle(), "{2}{R}");
        harness.passBothPriorities(); // Sizzle resolves.
        harness.passBothPriorities(); // Resolve Blood Hound's trigger.
        harness.handleMayAbilityChosen(player2, true);

        assertThat(hound.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Declining the damage trigger does not add counters")
    void damageDoesNotAddCountersWhenDeclined() {
        Permanent hound = harness.addToBattlefieldAndReturn(player2, new BloodHound());

        harness.castFromHand(player1, new Sizzle(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(hound.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("All +1/+1 counters are removed at its controller's end step")
    void removesCountersAtEndStep() {
        Permanent hound = harness.addToBattlefieldAndReturn(player1, new BloodHound());
        hound.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        hound.setCounterCount(CounterType.CHARGE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(hound.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(hound.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage dealt by its controller may put that much +1/+1 counters on Blood Hound")
    void damageFromItsControllerAlsoAddsCounters() {
        Permanent hound = harness.addToBattlefieldAndReturn(player1, new BloodHound());
        harness.addToBattlefieldAndReturn(player1, new ShockTroops());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities(); // Shock Troops' ability resolves.
        harness.passBothPriorities(); // Resolve Blood Hound's trigger.
        harness.handleMayAbilityChosen(player1, true);

        assertThat(hound.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Counters remain until its controller's end step")
    void opponentEndStepDoesNotRemoveCounters() {
        Permanent hound = harness.addToBattlefieldAndReturn(player1, new BloodHound());
        hound.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(hound.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
}
