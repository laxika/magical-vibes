package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DivebomberGriffin.class})
class DivebomberGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and deals 3 damage to an attacking creature")
    void sacrificesItselfAndDamagesAttacker() {
        addCreatureReady(player1, new DivebomberGriffin());
        Permanent attacker = addCombatCreature(player2, true, false);

        harness.activateAbility(player1, 0, null, attacker.getId());

        harness.assertInGraveyard(player1, "Divebomber Griffin");
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target a blocking creature")
    void damagesBlocker() {
        addCreatureReady(player1, new DivebomberGriffin());
        Permanent blocker = addCombatCreature(player2, false, true);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetIdleCreature() {
        addCreatureReady(player1, new DivebomberGriffin());
        Permanent idle = addCombatCreature(player2, false, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, idle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    @Test
    @DisplayName("Cannot activate while Divebomber Griffin has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new DivebomberGriffin());
        Permanent attacker = addCombatCreature(player2, true, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Does not damage a target that stops attacking before resolution")
    void doesNotDamageTargetThatStopsAttackingBeforeResolution() {
        addCreatureReady(player1, new DivebomberGriffin());
        Permanent attacker = addCombatCreature(player2, true, false);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Can deal lethal damage to its controller's blocking creature")
    void canDamageOwnBlockingCreature() {
        addCreatureReady(player1, new DivebomberGriffin());
        Permanent blocker = addCombatCreature(player1, false, true);

        harness.activateAbility(player1, 0, null, blocker.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(blocker);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Divebomber Griffin");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot pay the tap cost while already tapped")
    void cannotActivateWhileTapped() {
        Permanent griffin = addCreatureReady(player1, new DivebomberGriffin());
        griffin.tap();
        Permanent attacker = addCombatCreature(player2, true, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        harness.assertOnBattlefield(player1, "Divebomber Griffin");
        harness.assertNotInGraveyard(player1, "Divebomber Griffin");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target itself while blocking but is sacrificed before resolution")
    void canTargetItselfWhileBlocking() {
        Permanent griffin = addCombatCreature(player1, false, true);

        harness.activateAbility(player1, 0, null, griffin.getId());

        harness.assertNotOnBattlefield(player1, "Divebomber Griffin");
        harness.assertInGraveyard(player1, "Divebomber Griffin");
        harness.passBothPriorities();

        assertThat(griffin.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not damage a target that stops blocking before resolution")
    void doesNotDamageTargetThatStopsBlockingBeforeResolution() {
        addCreatureReady(player1, new DivebomberGriffin());
        Permanent blocker = addCombatCreature(player2, false, true);

        harness.activateAbility(player1, 0, null, blocker.getId());
        blocker.setBlocking(false);
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Divebomber Griffin");
        harness.assertInGraveyard(player1, "Divebomber Griffin");
    }

    private Permanent addCombatCreature(Player player, boolean attacking, boolean blocking) {
        Permanent creature = addCreatureReady(player, new DivebomberGriffin());
        creature.setAttacking(attacking);
        creature.setBlocking(blocking);
        return creature;
    }
}
