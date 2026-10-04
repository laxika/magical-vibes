package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.t.TragicSlip;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ErdwalRipper.class, SerraAngel.class, TragicSlip.class})
class ErdwalRipperTest extends BaseCardTest {

    private Permanent addReadyRipper() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new ErdwalRipper());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when dealing combat damage to a player")
    void getsCounterOnCombatDamage() {
        Permanent ripper = addReadyRipper();
        ripper.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 18);
        assertThat(ripper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(ripper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals increased combat damage after getting a +1/+1 counter")
    void dealsMoreDamageWithCounter() {
        Permanent ripper = addReadyRipper();
        ripper.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        ripper.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 17);

        harness.passBothPriorities();
        assertThat(ripper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("No counter when blocked and dealing no damage to a player")
    void noCounterWhenBlocked() {
        Permanent ripper = addReadyRipper();
        ripper.setAttacking(true);
        harness.setLife(player2, 20);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Erdwal Ripper");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Zero power deals no damage and does not trigger a counter")
    void zeroPowerDoesNotTrigger() {
        Permanent ripper = addReadyRipper();
        ripper.setPowerModifier(-2);
        ripper.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(ripper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each Ripper gets its own counter while a nonattacking Ripper gets none")
    void countersBelongToEachDamageSource() {
        Permanent first = addReadyRipper();
        Permanent second = addReadyRipper();
        Permanent nonattacker = addReadyRipper();
        first.setAttacking(true);
        second.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();
        harness.assertLife(player2, 16);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonattacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Removing the source before its trigger resolves does not put a counter on another Ripper")
    void removedSourceDoesNotGiveAnotherRipperACounter() {
        Permanent attacker = addReadyRipper();
        Permanent other = addReadyRipper();
        attacker.setAttacking(true);
        harness.setHand(player2, List.of(new TragicSlip()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.setLife(player2, 20);

        resolveCombat();
        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player2, 0, attacker.getId());
        harness.assertInGraveyard(player1, "Erdwal Ripper");
        resolveAllTriggers();

        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Haste allows a newly entered Ripper to attack and earn a counter")
    void newlyEnteredRipperCanAttack() {
        Permanent ripper = harness.addToBattlefieldAndReturn(player1, new ErdwalRipper());
        ripper.setSummoningSick(true);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(ripper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
