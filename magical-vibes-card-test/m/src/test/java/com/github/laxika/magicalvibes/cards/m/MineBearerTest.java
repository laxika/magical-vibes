package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MineBearer.class, PygmyRazorback.class})
class MineBearerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and destroys the attacking creature")
    void sacrificesItselfAndDestroysAttacker() {
        addReadyMineBearer(player1);
        Permanent attacker = addAttacker(player2);

        harness.activateAbility(player1, 0, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mine Bearer");
        harness.assertInGraveyard(player1, "Mine Bearer");
        harness.assertNotOnBattlefield(player2, "Pygmy Razorback");
        harness.assertInGraveyard(player2, "Pygmy Razorback");
    }

    @Test
    @DisplayName("Does not destroy a target that stops attacking before resolution")
    void doesNotDestroyTargetThatStopsAttackingBeforeResolution() {
        addReadyMineBearer(player1);
        Permanent attacker = addAttacker(player2);

        harness.activateAbility(player1, 0, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Pygmy Razorback");
        harness.assertNotInGraveyard(player2, "Pygmy Razorback");
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttacker() {
        addReadyMineBearer(player1);
        Permanent creature = addCreatureReady(player2, new PygmyRazorback());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately while destruction waits for resolution")
    void sacrificeIsPaidBeforeResolution() {
        addReadyMineBearer(player1);
        Permanent attacker = addAttacker(player2);

        harness.activateAbility(player1, 0, 0, null, attacker.getId());

        harness.assertNotOnBattlefield(player1, "Mine Bearer");
        harness.assertInGraveyard(player1, "Mine Bearer");
        harness.assertOnBattlefield(player2, "Pygmy Razorback");
        harness.assertNotInGraveyard(player2, "Pygmy Razorback");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Pygmy Razorback");
        harness.assertInGraveyard(player2, "Pygmy Razorback");
    }

    @Test
    @DisplayName("Can destroy an attacking creature its controller controls")
    void canDestroyOwnAttackingCreature() {
        addReadyMineBearer(player1);
        Permanent attacker = addAttacker(player1);

        harness.activateAbility(player1, 0, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mine Bearer");
        harness.assertNotOnBattlefield(player1, "Pygmy Razorback");
        harness.assertInGraveyard(player1, "Pygmy Razorback");
    }

    @Test
    @DisplayName("Cannot activate while tapped and does not sacrifice itself")
    void cannotActivateWhileTapped() {
        Permanent bearer = addReadyMineBearer(player1);
        bearer.tap();
        Permanent attacker = addAttacker(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Mine Bearer");
        harness.assertNotInGraveyard(player1, "Mine Bearer");
        harness.assertOnBattlefield(player2, "Pygmy Razorback");
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness and does not sacrifice itself")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new MineBearer());
        Permanent attacker = addAttacker(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Mine Bearer");
        harness.assertNotInGraveyard(player1, "Mine Bearer");
        harness.assertOnBattlefield(player2, "Pygmy Razorback");
    }

    private Permanent addReadyMineBearer(Player player) {
        return addCreatureReady(player, new MineBearer());
    }

    private Permanent addAttacker(Player player) {
        Permanent attacker = addCreatureReady(player, new PygmyRazorback());
        attacker.setAttacking(true);
        return attacker;
    }
}
