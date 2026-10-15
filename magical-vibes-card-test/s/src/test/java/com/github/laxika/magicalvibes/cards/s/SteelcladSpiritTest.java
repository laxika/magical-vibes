package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteelcladSpirit.class, GloriousAnthem.class, GrizzlyBears.class})
class SteelcladSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack without an enchantment entering under its controller's control")
    void cannotAttackWithoutEnchantmentTrigger() {
        addSpirit();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Can attack this turn after an enchantment enters under its controller's control")
    void canAttackAfterAllyEnchantmentEnters() {
        Permanent spirit = addSpirit();
        castAnthem(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(spirit)));

        assertThat(spirit.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The attack permission expires at the end of the turn")
    void attackPermissionExpiresAtEndOfTurn() {
        Permanent spirit = addSpirit();
        castAnthem(player1);

        assertThat(als.canAttack(gd, spirit, player1.getId())).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(als.canAttack(gd, spirit, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("An opponent's enchantment does not grant the attack permission")
    void opponentEnchantmentDoesNotTrigger() {
        Permanent spirit = addSpirit();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(spirit))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Attack permission begins only when the enchantment-entry trigger resolves")
    void permissionRequiresTriggerResolution() {
        Permanent spirit = addSpirit();
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        assertThat(als.canAttack(gd, spirit, player1.getId())).isFalse();

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(als.canAttack(gd, spirit, player1.getId())).isFalse();

        harness.passBothPriorities();
        assertThat(als.canAttack(gd, spirit, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("The enchantment trigger does not bypass summoning sickness")
    void permissionDoesNotBypassSummoningSickness() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new SteelcladSpirit());
        spirit.setSummoningSick(true);
        castAnthem(player1);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("A non-enchantment creature entering does not grant attack permission")
    void nonEnchantmentDoesNotTrigger() {
        Permanent spirit = addSpirit();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(als.canAttack(gd, spirit, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("An enchantment already present does not grant permission to a later Spirit")
    void enchantmentEnteringBeforeSpiritDoesNotGrantPermission() {
        castAnthem(player1);
        Permanent spirit = addSpirit();

        assertThat(als.canAttack(gd, spirit, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("Attack permission does not bypass being tapped")
    void permissionDoesNotBypassBeingTapped() {
        Permanent spirit = addSpirit();
        castAnthem(player1);
        spirit.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    private Permanent addSpirit() {
        return addCreatureReady(player1, new SteelcladSpirit());
    }

    private void castAnthem(Player player) {
        harness.setHand(player, List.of(new GloriousAnthem()));
        harness.addMana(player, ManaColor.WHITE, 3);
        harness.castEnchantment(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
