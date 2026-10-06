package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OgreMenial.class})
class OgreMenialTest extends BaseCardTest {

    @Test
    @DisplayName("Activating pump ability gives +1/+0 until end of turn")
    void pumpGivesPlusOneZero() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new OgreMenial());
        perm.setSummoningSick(false);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(perm.getPowerModifier()).isEqualTo(1);
        assertThat(perm.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Multiple pump activations stack")
    void multiplePumpsStack() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new OgreMenial());
        perm.setSummoningSick(false);

        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(perm.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Pumped Ogre Menial deals poison counters equal to boosted power")
    void pumpedDealsPoisonCounters() {
        harness.setLife(player2, 20);

        Permanent perm = harness.addToBattlefieldAndReturn(player1, new OgreMenial());
        perm.setSummoningSick(false);

        // Pump twice to give it 2 power (base 0 + 2)
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        perm.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Life should remain unchanged (infect deals poison, not life loss)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Poison counters should equal boosted power (2)
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Unpumped Ogre Menial (0 power) deals no poison counters")
    void unpumpedDealsNoPoisonCounters() {
        harness.setLife(player2, 20);

        Permanent perm = harness.addToBattlefieldAndReturn(player1, new OgreMenial());
        perm.setSummoningSick(false);
        perm.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Pump can be activated while tapped and summoning sick")
    void pumpsWhileTappedAndSummoningSick() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new OgreMenial());
        perm.setSummoningSick(true);
        perm.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(perm.getPowerModifier()).isZero();
        harness.passBothPriorities();

        assertThat(perm.getPowerModifier()).isEqualTo(1);
        assertThat(perm.getToughnessModifier()).isZero();
        assertThat(perm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pump expires at cleanup")
    void pumpExpiresAtCleanup() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new OgreMenial());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(perm.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(perm.getPowerModifier()).isZero();
        assertThat(perm.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Infect damage puts lasting minus counters on a blocker")
    void infectDamagesBlockerWithCounters() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new OgreMenial());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new OgreMenial());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.assertOnBattlefield(player2, "Ogre Menial");

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(attacker.getPowerModifier()).isZero();
    }
}
