package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.Scarecrow;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrothersOfFire.class, BogRats.class, Scarecrow.class})
class BrothersOfFireTest extends BaseCardTest {

    // ===== Activating ability =====

    @Test
    @DisplayName("Activating ability targeting player puts it on the stack")
    void activatingTargetingPlayerPutsOnStack() {
        addReadyBrothers(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Activating ability consumes {1}{R}{R}")
    void activatingConsumesMana() {
        addReadyBrothers(player1);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, player2.getId());

        // 4 - 3 = 1 mana remaining
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    // ===== Dealing damage to player =====

    @Test
    @DisplayName("Deals 1 damage to target player and 1 damage to controller")
    void deals1DamageToPlayerAnd1ToController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addReadyBrothers(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Can target self — takes both 1 target damage and 1 controller damage")
    void canTargetSelf() {
        harness.setLife(player1, 20);
        addReadyBrothers(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // 20 - 1 (target damage) - 1 (controller damage) = 18
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    // ===== Dealing damage to creature =====

    @Test
    @DisplayName("Deals 1 damage to target creature, destroying a 1/1, and 1 damage to controller")
    void deals1DamageDestroying1ToughnessAnd1ToController() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BogRats());
        addReadyBrothers(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Bog Rats");
        harness.assertInGraveyard(player2, "Bog Rats");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to target 2/2 creature, creature survives, controller takes 1 damage")
    void deals1DamageDoesNotKill2Toughness() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Scarecrow());
        addReadyBrothers(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player2, "Scarecrow");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyBrothers(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Ability fizzles if target creature is removed — controller takes no damage")
    void fizzlesIfTargetCreatureRemoved() {
        harness.setLife(player1, 20);
        addReadyBrothers(player1);
        harness.addMana(player1, ManaColor.RED, 3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Scarecrow());

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        // Controller does NOT take damage when ability fizzles
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Helpers =====

    private Permanent addReadyBrothers(Player player) {
        return addCreatureReady(player, new BrothersOfFire());
    }
}
