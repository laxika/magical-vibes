package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
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

@CardUsed({CharmedStray.class, PrimordialWurm.class})
class CharmedStrayTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each other Charmed Stray you control")
    void putsCountersOnOtherControlledCharmedStrays() {
        Permanent existingStray = harness.addToBattlefieldAndReturn(player1, new CharmedStray());
        Permanent unrelatedCreature = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        Permanent opponentStray = harness.addToBattlefieldAndReturn(player2, new CharmedStray());
        CharmedStray enteringCard = new CharmedStray();

        harness.setHand(player1, List.of(enteringCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent enteringStray = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(enteringCard.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(existingStray.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(enteringStray.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(unrelatedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentStray.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Entering alone does not put a counter on itself")
    void enteringAloneDoesNotReceiveCounter() {
        Permanent stray = harness.enterBattlefieldAndReturn(player1, new CharmedStray());

        harness.passBothPriorities();

        assertThat(stray.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Adds one counter to every other matching creature, including existing counters")
    void addsCountersToEveryOtherStray() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CharmedStray());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CharmedStray());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new CharmedStray());

        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Determines which creatures receive counters when the trigger resolves")
    void includesStrayPresentOnlyAtResolution() {
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new CharmedStray());
        Permanent laterStray = harness.addToBattlefieldAndReturn(player1, new CharmedStray());

        harness.passBothPriorities();

        assertThat(laterStray.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Lifelink gains life equal to combat damage including the counter bonus")
    void gainsLifeFromCombatDamageWithCounter() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new CharmedStray());
        harness.enterBattlefieldAndReturn(player1, new CharmedStray());
        harness.passBothPriorities();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Original trigger includes its source after it leaves and returns as a new permanent")
    void originalTriggerIncludesReturnedSource() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new CharmedStray());
        CharmedStray card = new CharmedStray();
        Permanent original = harness.enterBattlefieldAndReturn(player1, card);
        original.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Charmed Stray");

        gd.playerGraveyards.get(player1.getId()).remove(card);
        Permanent returned = harness.enterBattlefieldAndReturn(player1, card);
        assertThat(returned.getId()).isNotEqualTo(original.getId());

        harness.passBothPriorities();
        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();
        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
