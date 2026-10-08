package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VishKalBloodArbiter.class, GrizzlyBears.class})
class VishKalBloodArbiterTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature puts counters on Vish Kal equal to its effective power")
    void sacrificeCreatureAddsCountersEqualToPower() {
        Permanent vishKal = addCreatureReady(player1, new VishKalBloodArbiter());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(vishKal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Removing counters gives a target creature a matching temporary debuff")
    void removesCountersAndDebuffsTargetCreature() {
        Permanent vishKal = addCreatureReady(player1, new VishKalBloodArbiter());
        vishKal.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bears.getId());

        assertThat(vishKal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, bears)).isZero();
    }

    @Test
    @DisplayName("The second ability cannot target a noncreature")
    void secondAbilityCannotTargetNoncreature() {
        Permanent vishKal = addCreatureReady(player1, new VishKalBloodArbiter());
        vishKal.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vishKal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Vish Kal can sacrifice itself even when it is the only creature")
    void canSacrificeItself() {
        addCreatureReady(player1, new VishKalBloodArbiter());

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Vish Kal, Blood Arbiter");
        harness.assertInGraveyard(player1, "Vish Kal, Blood Arbiter");
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice is paid immediately and works while Vish Kal is tapped and summoning sick")
    void sacrificeDoesNotRequireTapOrHaste() {
        Permanent vishKal = addCreatureReady(player1, new VishKalBloodArbiter());
        vishKal.setSummoningSick(true);
        vishKal.tap();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(vishKal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(vishKal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing a zero-power creature puts no counters on Vish Kal")
    void zeroPowerSacrificeAddsNoCounters() {
        Permanent vishKal = addCreatureReady(player1, new VishKalBloodArbiter());
        vishKal.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setPowerModifier(-2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(vishKal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing a negative-power creature does not remove existing counters")
    void negativePowerSacrificeAddsNoCounters() {
        Permanent vishKal = addCreatureReady(player1, new VishKalBloodArbiter());
        vishKal.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setPowerModifier(-3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(vishKal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Removing all counters is legal with zero counters and does not debuff the target")
    void secondAbilityCanRemoveZeroCounters() {
        addCreatureReady(player1, new VishKalBloodArbiter());
        Permanent target = addCreatureReady(player2, new VishKalBloodArbiter());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("Vish Kal can target itself and removes only its +1/+1 counters")
    void secondAbilityCanTargetItselfAndLeavesOtherCounters() {
        Permanent vishKal = addCreatureReady(player1, new VishKalBloodArbiter());
        vishKal.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        vishKal.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, 1, null, vishKal.getId());
        harness.passBothPriorities();

        assertThat(vishKal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(vishKal.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, vishKal)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vishKal)).isEqualTo(3);
    }

    @Test
    @DisplayName("The debuff uses counters removed as its cost, ignoring counters gained in response")
    void debuffSnapshotsRemovedCounters() {
        Permanent vishKal = addCreatureReady(player1, new VishKalBloodArbiter());
        vishKal.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new VishKalBloodArbiter());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(vishKal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        harness.passBothPriorities();

        assertThat(vishKal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("The debuff resolves after Vish Kal leaves the battlefield and does not gain life")
    void debuffResolvesWithoutSourceAndDoesNotDealDamage() {
        Permanent vishKal = addCreatureReady(player1, new VishKalBloodArbiter());
        vishKal.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = addCreatureReady(player2, new VishKalBloodArbiter());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Vish Kal, Blood Arbiter");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Removed counters are not refunded when the target leaves before resolution")
    void invalidatedTargetDoesNotRefundCounters() {
        Permanent vishKal = addCreatureReady(player1, new VishKalBloodArbiter());
        vishKal.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = addCreatureReady(player2, new VishKalBloodArbiter());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.activateAbility(player2, 0, 0, null, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Vish Kal, Blood Arbiter");
        assertThat(vishKal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, vishKal)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The debuff expires at cleanup rather than leaving -1/-1 counters")
    void debuffExpiresAtCleanup() {
        Permanent vishKal = addCreatureReady(player1, new VishKalBloodArbiter());
        vishKal.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = addCreatureReady(player2, new VishKalBloodArbiter());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(vishKal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking Vish Kal")
    void groundCreatureCannotBlock() {
        Permanent vishKal = addCreatureReady(player1, new VishKalBloodArbiter());
        vishKal.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Combat damage gains life through lifelink")
    void unblockedCombatDamageGainsLife() {
        Permanent vishKal = addCreatureReady(player1, new VishKalBloodArbiter());
        vishKal.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 15);
    }
}
