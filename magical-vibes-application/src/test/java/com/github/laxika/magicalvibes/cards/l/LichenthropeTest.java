package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Fireblast;
import com.github.laxika.magicalvibes.cards.m.MeliraSylvokOutcast;
import com.github.laxika.magicalvibes.cards.p.Python;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Lichenthrope.class, Fireblast.class, Python.class})
class LichenthropeTest extends BaseCardTest {

    @Test
    @DisplayName("Fireblast damage is replaced with -1/-1 counters")
    void fireblastDamageReplacedWithCounters() {
        harness.addToBattlefield(player2, new Lichenthrope());
        harness.setHand(player1, List.of(new Fireblast()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID id = harness.getPermanentId(player2, "Lichenthrope");
        harness.castAndResolveInstant(player1, 0, id);

        harness.assertOnBattlefield(player2, "Lichenthrope");
        Permanent lichenthrope = findPermanent(player2, "Lichenthrope");
        assertThat(lichenthrope.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(lichenthrope.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Combat damage is replaced with -1/-1 counters")
    void combatDamageReplacedWithCounters() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new Lichenthrope());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new Python());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertOnBattlefield(player2, "Lichenthrope");
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @CardUsed(MeliraSylvokOutcast.class)
    @DisplayName("Damage replacement respects an effect that forbids -1/-1 counters")
    void damageReplacementRespectsMinusCounterRestriction() {
        harness.addToBattlefield(player2, new MeliraSylvokOutcast());
        Permanent lichenthrope = harness.addToBattlefieldAndReturn(player2, new Lichenthrope());
        harness.setHand(player1, List.of(new Fireblast()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, lichenthrope.getId());

        harness.assertOnBattlefield(player2, "Lichenthrope");
        assertThat(lichenthrope.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(lichenthrope.getMarkedDamage()).isZero();
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
