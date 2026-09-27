package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(DivebomberGriffin.class)
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

    private Permanent addCombatCreature(Player player, boolean attacking, boolean blocking) {
        Permanent creature = addCreatureReady(player, new DivebomberGriffin());
        creature.setAttacking(attacking);
        creature.setBlocking(blocking);
        return creature;
    }
}
