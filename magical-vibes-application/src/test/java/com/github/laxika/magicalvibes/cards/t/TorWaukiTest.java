package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.z.ZephyrFalcon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TorWauki.class, ZephyrFalcon.class, Tolaria.class})
class TorWaukiTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a target attacking creature")
    void damagesAttacker() {
        addReadyTorWauki(player1);
        Permanent attacker = addCombatCreature(player2, true, false);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Zephyr Falcon");
        harness.assertInGraveyard(player2, "Zephyr Falcon");
    }

    @Test
    @DisplayName("Deals 2 damage to a target blocking creature")
    void damagesBlocker() {
        addReadyTorWauki(player1);
        Permanent blocker = addCombatCreature(player2, false, true);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Zephyr Falcon");
        harness.assertInGraveyard(player2, "Zephyr Falcon");
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetIdleCreature() {
        addReadyTorWauki(player1);
        Permanent idle = addCombatCreature(player2, false, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, idle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    private Permanent addReadyTorWauki(Player player) {
        return addCreatureReady(player, new TorWauki());
    }

    private Permanent addCombatCreature(Player player, boolean attacking, boolean blocking) {
        Permanent creature = addCreatureReady(player, new ZephyrFalcon());
        creature.setAttacking(attacking);
        creature.setBlocking(blocking);
        return creature;
    }

    @Test
    @DisplayName("Deals exactly 2 damage to a target attacking creature")
    void dealsExactlyTwoDamage() {
        Permanent torWauki = addReadyTorWauki(player1);
        Permanent attacker = addCreatureReady(player2, new TorWauki());
        attacker.setAttacking(true);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(torWauki.isTapped()).isTrue();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Tor Wauki");
    }

    @Test
    @DisplayName("Can target a blocking creature controlled by Tor Wauki's controller")
    void canTargetOwnBlockingCreature() {
        addReadyTorWauki(player1);
        Permanent blocker = addCombatCreature(player1, false, true);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Zephyr Falcon");
        harness.assertInGraveyard(player1, "Zephyr Falcon");
    }

    @Test
    @DisplayName("Does not damage a target that stops attacking before resolution")
    void targetMustStillBeAttackingOnResolution() {
        addReadyTorWauki(player1);
        Permanent attacker = addCombatCreature(player2, true, false);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Zephyr Falcon");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        addReadyTorWauki(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Tolaria());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Cannot activate while Tor Wauki has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new TorWauki());
        Permanent attacker = addCombatCreature(player2, true, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }
}
