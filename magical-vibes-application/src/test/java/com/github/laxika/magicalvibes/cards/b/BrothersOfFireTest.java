package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({BrothersOfFire.class, BogRats.class, GrizzlyBears.class, ChandraNalaar.class})
class BrothersOfFireTest extends BaseCardTest {
    @Test
    @DisplayName("Activating ability targeting player puts it on the stack")
    void activatingTargetingPlayerPutsOnStack() {
        Permanent brothers = addReadyBrothers(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(brothers.isTapped()).isFalse();
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
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Can target self — takes both 1 target damage and 1 controller damage")
    void canTargetSelf() {
        harness.setLife(player1, 20);
        addReadyBrothers(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        // 20 - 1 (target damage) - 1 (controller damage) = 18
        harness.assertLife(player1, 18);
    }
    @Test
    @DisplayName("Deals 1 damage to target creature, destroying a 1/1, and 1 damage to controller")
    void deals1DamageDestroying1ToughnessAnd1ToController() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BogRats());
        addReadyBrothers(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bog Rats");
        harness.assertInGraveyard(player2, "Bog Rats");
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Deals 1 damage to target 2/2 creature, creature survives, controller takes 1 damage")
    void deals1DamageDoesNotKill2Toughness() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addReadyBrothers(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Deals 1 damage to a planeswalker and 1 damage to controller")
    void deals1DamageToPlaneswalkerAnd1ToController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        target.setCounterCount(CounterType.LOYALTY, 3);
        addReadyBrothers(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }
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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        // Controller does NOT take damage when ability fizzles
        harness.assertLife(player1, 20);
    }
    private Permanent addReadyBrothers(Player player) {
        return addCreatureReady(player, new BrothersOfFire());
    }
}
