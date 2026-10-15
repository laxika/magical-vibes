package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.ShieldedAetherThief;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeftDismissal.class, ShieldedAetherThief.class})
class DeftDismissalTest extends BaseCardTest {

    @Test
    void dealsThreeDamageToSingleAttacker() {
        Permanent attacker = addCombatCreature(true, false);
        cast(Map.of(attacker.getId(), 3));

        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void dividesDamageAmongAttackerAndBlocker() {
        Permanent attacker = addCombatCreature(true, false);
        Permanent blocker = addCombatCreature(false, true);
        cast(Map.of(attacker.getId(), 2, blocker.getId(), 1));

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void dividesDamageAmongThreeCombatCreatures() {
        Permanent first = addCombatCreature(true, false);
        Permanent second = addCombatCreature(true, false);
        Permanent third = addCombatCreature(false, true);
        cast(Map.of(first.getId(), 1, second.getId(), 1, third.getId(), 1));

        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
        assertThat(third.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void cannotTargetNonCombatCreature() {
        Permanent creature = addCreatureReady(player2, new ShieldedAetherThief());
        harness.setHand(player1, List.of(new DeftDismissal()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(creature.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageAssignmentsMustSumToThree() {
        Permanent attacker = addCombatCreature(true, false);
        harness.setHand(player1, List.of(new DeftDismissal()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(attacker.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotAssignZeroDamageToATarget() {
        Permanent first = addCombatCreature(true, false);
        Permanent second = addCombatCreature(false, true);
        harness.setHand(player1, List.of(new DeftDismissal()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                Map.of(first.getId(), 3, second.getId(), 0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new DeftDismissal()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(player2.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotDamageCreatureThatLeavesCombatBeforeResolution() {
        Permanent attacker = addCombatCreature(true, false);
        harness.setHand(player1, List.of(new DeftDismissal()));
        addMana();
        harness.castInstant(player1, 0, Map.of(attacker.getId(), 3));
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotRedistributeDamageWhenOneTargetLeavesCombat() {
        Permanent attacker = addCombatCreature(true, false);
        Permanent blocker = addCombatCreature(false, true);
        harness.setHand(player1, List.of(new DeftDismissal()));
        addMana();
        harness.castInstant(player1, 0, Map.of(attacker.getId(), 2, blocker.getId(), 1));
        blocker.setBlocking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    void canTargetControllersAttackingCreature() {
        Permanent attacker = addCreatureReady(player1, new ShieldedAetherThief());
        attacker.setAttacking(true);
        cast(Map.of(attacker.getId(), 3));

        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    private void cast(Map<java.util.UUID, Integer> damageAssignments) {
        harness.setHand(player1, List.of(new DeftDismissal()));
        addMana();
        harness.castInstant(player1, 0, damageAssignments);
        harness.passBothPriorities();
    }

    private Permanent addCombatCreature(boolean attacking, boolean blocking) {
        Permanent creature = addCreatureReady(player2, new ShieldedAetherThief());
        creature.setAttacking(attacking);
        creature.setBlocking(blocking);
        return creature;
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
