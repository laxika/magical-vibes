package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RuinationWurm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DutifulThrull.class, RuinationWurm.class})
class DutifulThrullTest extends BaseCardTest {

    @Test
    @DisplayName("Activating regeneration records Dutiful Thrull as its source")
    void activatingRegenerationTargetsSelf() {
        Permanent thrull = addDutifulThrullReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(thrull.getId());
    }

    @Test
    @DisplayName("Resolving Dutiful Thrull's regeneration grants a regeneration shield")
    void resolvingRegenerationGrantsShield() {
        addDutifulThrullReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent thrull = findPermanent(player1, "Dutiful Thrull");
        assertThat(thrull.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Dutiful Thrull's regeneration shield saves it from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent thrull = addDutifulThrullReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        thrull.setBlocking(true);
        thrull.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new RuinationWurm());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dutiful Thrull");
        Permanent survivingThrull = findPermanent(player1, "Dutiful Thrull");
        assertThat(survivingThrull.isTapped()).isTrue();
        assertThat(survivingThrull.getRegenerationShield()).isZero();
        assertThat(survivingThrull.getMarkedDamage()).isZero();
        assertThat(survivingThrull.isBlocking()).isFalse();
        assertThat(survivingThrull.getBlockingTargets()).isEmpty();
    }

    @Test
    @DisplayName("Dutiful Thrull dies from lethal combat damage without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent thrull = addDutifulThrullReady(player1);
        thrull.setBlocking(true);
        thrull.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new RuinationWurm());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dutiful Thrull");
        harness.assertInGraveyard(player1, "Dutiful Thrull");
    }

    private Permanent addDutifulThrullReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new DutifulThrull());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent thrull = harness.addToBattlefieldAndReturn(player1, new DutifulThrull());
        thrull.setSummoningSick(true);
        thrull.setTapped(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(thrull.getRegenerationShield()).isEqualTo(1);
        assertThat(thrull.isTapped()).isTrue();
    }

    @Test
    void activationRequiresBlackMana() {
        addDutifulThrullReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void multipleActivationsProtectAgainstSeparateDestructions() {
        Permanent thrull = addDutifulThrullReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(thrull.isTapped()).isFalse();
        assertThat(thrull.getRegenerationShield()).isEqualTo(2);
        thrull.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player1, "Dutiful Thrull");
        assertThat(thrull.getRegenerationShield()).isEqualTo(1);
        assertThat(thrull.getMarkedDamage()).isZero();

        thrull.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player1, "Dutiful Thrull");
        assertThat(thrull.getRegenerationShield()).isZero();
        assertThat(thrull.getMarkedDamage()).isZero();

        thrull.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Dutiful Thrull");
    }

    @Test
    void regenerationCannotSaveCreatureWithZeroToughness() {
        Permanent thrull = addDutifulThrullReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        thrull.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Dutiful Thrull");
        harness.assertInGraveyard(player1, "Dutiful Thrull");
    }
}
