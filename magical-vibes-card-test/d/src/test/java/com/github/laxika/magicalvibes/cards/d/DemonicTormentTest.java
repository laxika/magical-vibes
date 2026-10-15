package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonicTorment.class, AzureDrake.class, DAvenantArcher.class})
class DemonicTormentTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can't attack")
    void enchantedCreatureCannotAttack() {
        Permanent creature = addCreatureReady(player1, new AzureDrake());
        castDemonicTorment(player1, creature);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("An opponent's enchanted creature can't attack")
    void opponentsEnchantedCreatureCannotAttack() {
        Permanent creature = addCreatureReady(player2, new AzureDrake());
        castDemonicTorment(player1, creature);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("An opponent's enchanted blocker deals no combat damage")
    void opponentsEnchantedBlockerDealsNoCombatDamage() {
        Permanent blocker = addCreatureReady(player2, new AzureDrake());
        castDemonicTorment(player1, blocker);
        Permanent attacker = addCreatureReady(player1, new AzureDrake());

        declareAttackersAndPrepareBlockers(player1, List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing Demonic Torment restores attacking and combat damage")
    void removingAuraRestoresAttackingAndDamage() {
        Permanent creature = addCreatureReady(player1, new AzureDrake());
        castDemonicTorment(player1, creature);
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Demonic Torment"));
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new DemonicTorment());
        harness.setHand(player1, List.of(new DemonicTorment()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted creature can block, but its combat damage is prevented")
    void blocksWithoutDealingCombatDamage() {
        Permanent enchanted = addCreatureReady(player1, new AzureDrake());
        castDemonicTorment(player1, enchanted);

        Permanent attacker = addCreatureReady(player2, new AzureDrake());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(enchanted.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Demonic Torment does not prevent noncombat damage from the enchanted creature")
    void noncombatDamageStillApplies() {
        Permanent enchanted = addCreatureReady(player1, new DAvenantArcher());
        Permanent target = addCreatureReady(player2, new AzureDrake());
        target.setAttacking(true);
        castDemonicTorment(player1, enchanted);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    private void castDemonicTorment(Player controller, Permanent target) {
        harness.forceActivePlayer(controller);
        harness.setHand(controller, List.of(new DemonicTorment()));
        harness.addMana(controller, ManaColor.BLACK, 1);
        harness.addMana(controller, ManaColor.COLORLESS, 2);
        harness.castEnchantment(controller, 0, target.getId());
        harness.passBothPriorities();
    }
}
