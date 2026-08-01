package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LichenthropeTest extends BaseCardTest {

    @Test
    @DisplayName("Shock damage is replaced with -1/-1 counters")
    void shockDamageReplacedWithCounters() {
        harness.addToBattlefield(player2, new Lichenthrope());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID id = harness.getPermanentId(player2, "Lichenthrope");
        harness.castInstant(player1, 0, id);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Lichenthrope");
        Permanent lichenthrope = findPermanent(player2, "Lichenthrope");
        assertThat(lichenthrope.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(lichenthrope.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Combat damage is replaced with -1/-1 counters")
    void combatDamageReplacedWithCounters() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new Lichenthrope());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Lichenthrope");
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("At the beginning of your upkeep, remove a -1/-1 counter")
    void upkeepRemovesMinusCounter() {
        Permanent lichenthrope = harness.addToBattlefieldAndReturn(player1, new Lichenthrope());
        lichenthrope.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(lichenthrope.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's upkeep does not remove a -1/-1 counter")
    void opponentUpkeepDoesNotRemoveCounter() {
        Permanent lichenthrope = harness.addToBattlefieldAndReturn(player1, new Lichenthrope());
        lichenthrope.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(lichenthrope.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }
}
