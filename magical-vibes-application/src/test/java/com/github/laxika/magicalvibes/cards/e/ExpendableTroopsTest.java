package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.y.YavimayaWurm;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExpendableTroops.class, YavimayaWurm.class})
class ExpendableTroopsTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and deals 2 damage to an attacking creature")
    void sacrificesItselfAndDamagesAttacker() {
        addReadyTroops(player1);
        Permanent attacker = addCombatCreature(player2, true, false);

        harness.activateAbility(player1, 0, null, attacker.getId());

        harness.assertInGraveyard(player1, "Expendable Troops");
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target a blocking creature")
    void damagesBlocker() {
        addReadyTroops(player1);
        Permanent blocker = addCombatCreature(player2, false, true);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not damage a target that stops attacking or blocking before resolution")
    void doesNotDamageTargetThatLeavesCombatBeforeResolution() {
        addReadyTroops(player1);
        Permanent attacker = addCombatCreature(player2, true, false);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetIdleCreature() {
        addReadyTroops(player1);
        Permanent idle = addCombatCreature(player2, false, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, idle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    @Test
    @DisplayName("Cannot activate while Expendable Troops has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new ExpendableTroops());
        Permanent attacker = addCombatCreature(player2, true, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    private Permanent addReadyTroops(Player player) {
        return addCreatureReady(player, new ExpendableTroops());
    }

    private Permanent addCombatCreature(Player player, boolean attacking, boolean blocking) {
        Permanent creature = addCreatureReady(player, new YavimayaWurm());
        creature.setAttacking(attacking);
        creature.setBlocking(blocking);
        return creature;
    }
}
