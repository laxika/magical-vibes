package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.z.Zombify;
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

@CardUsed({NineLivesFamiliar.class, Zombify.class})
class NineLivesFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with eight revival counters when cast")
    void entersWithCountersWhenCast() {
        harness.setHand(player1, List.of(new NineLivesFamiliar()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent familiar = findPermanent(player1, "Nine-Lives Familiar");
        assertThat(familiar.getCounterCount(CounterType.REVIVAL)).isEqualTo(8);
    }

    @Test
    @DisplayName("Returns at the next end step with one fewer revival counter")
    void returnsWithOneFewerCounter() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new NineLivesFamiliar());
        familiar.setCounterCount(CounterType.REVIVAL, 3);
        kill(familiar);

        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Nine-Lives Familiar");
        assertThat(returned.getCounterCount(CounterType.REVIVAL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not return when it dies without a revival counter")
    void doesNotReturnWithoutCounter() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new NineLivesFamiliar());
        kill(familiar);

        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertNotOnBattlefield(player1, "Nine-Lives Familiar");
        harness.assertInGraveyard(player1, "Nine-Lives Familiar");
    }

    private void kill(Permanent familiar) {
        familiar.setMarkedDamage(familiar.getEffectiveToughness());
        harness.runStateBasedActions();
    }

    @Test
    @DisplayName("Entering without being cast does not add revival counters")
    void entersWithoutCountersWhenNotCast() {
        Permanent familiar = harness.enterBattlefieldAndReturn(player1, new NineLivesFamiliar());

        assertThat(familiar.getCounterCount(CounterType.REVIVAL)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The last revival counter permits one final return")
    void returnsWithZeroCountersThenStaysDead() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new NineLivesFamiliar());
        familiar.setCounterCount(CounterType.REVIVAL, 1);
        kill(familiar);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Nine-Lives Familiar");
        assertThat(returned.getCounterCount(CounterType.REVIVAL)).isZero();
        kill(returned);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Nine-Lives Familiar");
    }

    @Test
    @DisplayName("Dying during an end step waits until the following end step")
    void deathDuringEndStepWaitsForNextEndStep() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new NineLivesFamiliar());
        familiar.setCounterCount(CounterType.REVIVAL, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        kill(familiar);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nine-Lives Familiar");
        harness.assertInGraveyard(player1, "Nine-Lives Familiar");
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Nine-Lives Familiar")
                .getCounterCount(CounterType.REVIVAL)).isEqualTo(1);
    }

    @Test
    @DisplayName("An old delayed return cannot return a card that was reanimated and died again")
    void doesNotReturnAfterLeavingAndReenteringGraveyard() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new NineLivesFamiliar());
        familiar.setCounterCount(CounterType.REVIVAL, 3);
        kill(familiar);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, familiar.getCard().getId());

        Permanent reanimated = findPermanent(player1, "Nine-Lives Familiar");
        assertThat(reanimated.getCounterCount(CounterType.REVIVAL)).isZero();
        kill(reanimated);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nine-Lives Familiar");
        harness.assertInGraveyard(player1, "Nine-Lives Familiar");
    }
}
