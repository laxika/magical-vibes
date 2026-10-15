package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.cards.j.Jump;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({MagebaneArmor.class, AzureDrake.class, RuneclawBear.class, LightningBolt.class, Jump.class})
class MagebaneArmorTest extends BaseCardTest {

    @Test
    void equipRequiresTwoMana() {
        Permanent armor = addArmorReady(player1);
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Not enough mana");
        assertThat(armor.getAttachedTo()).isNull();
    }

    @Test
    void cannotEquipOpponentsCreature() {
        Permanent armor = addArmorReady(player1);
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(armor.getAttachedTo()).isNull();
    }

    @Test
    void cannotEquipDuringCombat() {
        Permanent armor = addArmorReady(player1);
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("sorcery speed");
        assertThat(armor.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Resolving equip attaches Magebane Armor to target creature")
    void resolvingEquipAttaches() {
        Permanent armor = addArmorReady(player1);
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +2/+4")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);   // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6); // 2 + 4
    }

    @Test
    @DisplayName("Creature loses boost when Magebane Armor is removed")
    void creatureLosesBoostWhenArmorRemoved() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(armor);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature with flying loses flying")
    void equippedFlyingCreatureLosesFlying() {
        Permanent creature = addReadyFlyingCreature(player1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Creature regains flying when Magebane Armor is removed")
    void creatureRegainsFlyingWhenArmorRemoved() {
        Permanent creature = addReadyFlyingCreature(player1);
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(armor);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature without flying is unaffected by loses flying")
    void equippedNonFlyingCreatureUnaffected() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Noncombat damage to equipped creature is prevented")
    void noncombatDamageToEquippedCreatureIsPrevented() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        // Lightning Bolt targets the equipped creature
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        // Creature should survive — 3 noncombat damage prevented
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(creature.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Combat damage to equipped creature is NOT prevented")
    void combatDamageToEquippedCreatureIsNotPrevented() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Player1 has a creature equipped with Magebane Armor
        Permanent defender = addCreatureReady(player1, new RuneclawBear());
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(defender.getId());
        // 2/2 creature becomes 4/6 with armor

        // Player2 attacks with a creature
        Permanent attacker = addCreatureReady(player2, new RuneclawBear());
        attacker.setAttacking(true);

        // Defender blocks
        defender.setBlocking(true);
        defender.addBlockingTarget(0);

        resolveCombat(player2);

        // Attacker deals 2 combat damage to defender (4/6)
        // Combat damage is NOT prevented by Magebane Armor
        assertThat(defender.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Noncombat damage prevention does not apply to unequipped creature")
    void noncombatDamageNotPreventedOnUnequippedCreature() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        addArmorReady(player1); // Armor on battlefield but not attached

        // Lightning Bolt targets the unequipped creature
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        // 2/2 creature takes 3 damage and dies
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        harness.assertInGraveyard(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Moving Magebane Armor transfers all effects to new creature")
    void reEquipTransfersEffects() {
        Permanent armor = addArmorReady(player1);
        Permanent creature1 = addReadyFlyingCreature(player1);
        Permanent creature2 = addCreatureReady(player1, new RuneclawBear());
        armor.setAttachedTo(creature1.getId());

        // creature1 (Azure Drake 2/4) gets boost, loses flying
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(4);   // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature1)).isEqualTo(8); // 4 + 4
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.FLYING)).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        // creature1 loses boost, regains flying
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature1)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.FLYING)).isTrue();

        // creature2 (Runeclaw Bear 2/2) gets boost (no flying to lose)
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(4);   // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature2)).isEqualTo(6); // 2 + 4
    }

    @Test
    void movingArmorTransfersNoncombatDamagePrevention() {
        Permanent armor = addArmorReady(player1);
        Permanent first = addCreatureReady(player1, new AzureDrake());
        Permanent second = addCreatureReady(player1, new RuneclawBear());
        armor.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castInstant(player2, 0, second.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, second.getId());
        harness.passBothPriorities();
        assertThat(second.getMarkedDamage()).isZero();

        harness.castInstant(player2, 0, first.getId());
        harness.passBothPriorities();
        assertThat(first.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first, second);
    }

    @Test
    void creatureCanGainFlyingAfterArmorIsEquipped() {
        Permanent armor = addArmorReady(player1);
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Jump()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    void equippingArmorRemovesPreviouslyGrantedFlying() {
        Permanent armor = addArmorReady(player1);
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Jump()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    private Permanent addArmorReady(Player player) {
        return addCreatureReady(player, new MagebaneArmor());
    }

    private Permanent addReadyFlyingCreature(Player player) {
        return addCreatureReady(player, new AzureDrake());
    }
}
