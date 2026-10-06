package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed(ShatterskullCharger.class)
class ShatterskullChargerTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, returns to its owner's hand at its controller's end step")
    void returnsWithoutKicker() {
        harness.setHand(player1, List.of(new ShatterskullCharger()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shatterskull Charger");
        assertThat(findPermanent(player1, "Shatterskull Charger").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Shatterskull Charger");
        harness.assertInHand(player1, "Shatterskull Charger");
    }

    @Test
    @DisplayName("With kicker, enters with a +1/+1 counter and stays on the battlefield")
    void kickedChargerStaysOnBattlefield() {
        harness.setHand(player1, List.of(new ShatterskullCharger()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shatterskull Charger");
        assertThat(findPermanent(player1, "Shatterskull Charger").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Shatterskull Charger");
        harness.assertNotInHand(player1, "Shatterskull Charger");
    }

    @Test
    @DisplayName("A counter added in response prevents the return on resolution")
    void counterAddedInResponsePreventsReturn() {
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new ShatterskullCharger());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        charger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Shatterskull Charger");
        harness.assertNotInHand(player1, "Shatterskull Charger");
    }

    @Test
    @DisplayName("A counter at the beginning of the end step prevents the ability from triggering")
    void counterAtTriggerTimePreventsTrigger() {
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new ShatterskullCharger());
        charger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();

        charger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shatterskull Charger");
        harness.assertNotInHand(player1, "Shatterskull Charger");
    }

    @Test
    @DisplayName("Removing the kicked counter before the end step causes the creature to return")
    void kickedChargerReturnsAfterCounterRemoved() {
        harness.setHand(player1, List.of(new ShatterskullCharger()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        findPermanent(player1, "Shatterskull Charger").setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Shatterskull Charger");
        harness.assertInHand(player1, "Shatterskull Charger");
    }

    @Test
    @DisplayName("The creature does not return during an opponent's end step")
    void doesNotReturnDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new ShatterskullCharger());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Shatterskull Charger");
        harness.assertNotInHand(player1, "Shatterskull Charger");
    }
}
