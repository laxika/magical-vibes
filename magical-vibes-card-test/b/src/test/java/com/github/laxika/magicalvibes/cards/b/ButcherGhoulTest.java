package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.c.ControlMagic;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ButcherGhoul.class, LightningBolt.class, ControlMagic.class})
class ButcherGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield does not trigger undying")
    void enteringDoesNotTriggerUndying() {
        harness.addToBattlefield(player1, new ButcherGhoul());

        harness.assertOnBattlefield(player1, "Butcher Ghoul");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Undying returns Butcher Ghoul with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new ButcherGhoul());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, ghoul.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Butcher Ghoul");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.getEffectivePower()).isEqualTo(2);
        assertThat(returned.getEffectiveToughness()).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Butcher Ghoul");
    }

    @Test
    @DisplayName("Undying does not return Butcher Ghoul when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new ButcherGhoul());
        ghoul.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, ghoul.getId());

        harness.assertNotOnBattlefield(player1, "Butcher Ghoul");
        harness.assertInGraveyard(player1, "Butcher Ghoul");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller at death controls undying, but the creature returns to its owner")
    void stolenGhoulUndyingIsControlledByControllerAtDeath() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new ButcherGhoul());
        Permanent control = harness.addToBattlefieldAndReturn(player2, new ControlMagic());
        control.setAttachedTo(ghoul.getId());
        harness.runStateBasedActions();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, ghoul.getId());

        harness.assertInGraveyard(player1, "Butcher Ghoul");
        assertThat(gd.stack).singleElement().satisfies(trigger ->
                assertThat(trigger.getControllerId()).isEqualTo(player2.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Butcher Ghoul");
        harness.assertNotOnBattlefield(player2, "Butcher Ghoul");
        assertThat(findPermanent(player1, "Butcher Ghoul")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A Ghoul returned by undying stays dead when killed again")
    void returnedGhoulDoesNotReturnAgain() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new ButcherGhoul());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, ghoul.getId());
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Butcher Ghoul");
        harness.castAndResolveInstant(player1, 0, returned.getId());

        harness.assertNotOnBattlefield(player1, "Butcher Ghoul");
        harness.assertInGraveyard(player1, "Butcher Ghoul");
        assertThat(gd.stack).isEmpty();
    }
}
