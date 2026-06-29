package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventAllNoncombatDamageToAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MagebaneArmorTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Magebane Armor has static +2/+4 boost effect")
    void hasStaticBoostEffect() {
        MagebaneArmor card = new MagebaneArmor();

        StaticBoostEffect boost = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof StaticBoostEffect)
                .map(e -> (StaticBoostEffect) e)
                .findFirst().orElseThrow();
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(4);
    }

    @Test
    @DisplayName("Magebane Armor has static remove flying effect for equipped creature")
    void hasRemoveFlyingEffect() {
        MagebaneArmor card = new MagebaneArmor();

        RemoveKeywordEffect remove = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof RemoveKeywordEffect)
                .map(e -> (RemoveKeywordEffect) e)
                .findFirst().orElseThrow();
        assertThat(remove.keyword()).isEqualTo(Keyword.FLYING);
        assertThat(remove.scope()).isEqualTo(GrantScope.EQUIPPED_CREATURE);
    }

    @Test
    @DisplayName("Magebane Armor has noncombat damage prevention effect")
    void hasNoncombatDamagePreventionEffect() {
        MagebaneArmor card = new MagebaneArmor();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof PreventAllNoncombatDamageToAttachedCreatureEffect);
    }

    @Test
    @DisplayName("Magebane Armor has equip {2} ability")
    void hasEquipAbility() {
        MagebaneArmor card = new MagebaneArmor();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.getManaCost()).isEqualTo("{2}");
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getTargetFilter()).isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(ability.getTimingRestriction()).isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(ability.getEffects()).singleElement().isInstanceOf(EquipEffect.class);
    }

    // ===== Equip =====

    @Test
    @DisplayName("Resolving equip attaches Magebane Armor to target creature")
    void resolvingEquipAttaches() {
        Permanent armor = addArmorReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    // ===== Static effects: power/toughness boost =====

    @Test
    @DisplayName("Equipped creature gets +2/+4")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);   // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6); // 2 + 4
    }

    @Test
    @DisplayName("Creature loses boost when Magebane Armor is removed")
    void creatureLosesBoostWhenArmorRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(armor);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    // ===== Static effects: loses flying =====

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
        Permanent creature = addReadyCreature(player1);
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    // ===== Noncombat damage prevention =====

    @Test
    @DisplayName("Noncombat damage to equipped creature is prevented")
    void noncombatDamageToEquippedCreatureIsPrevented() {
        Permanent creature = addReadyCreature(player1);
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
        Permanent defender = addReadyCreature(player1);
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(defender.getId());
        // 2/2 creature becomes 4/6 with armor

        // Player2 attacks with a creature
        Permanent attacker = addReadyCreature(player2);
        attacker.setAttacking(true);

        // Defender blocks
        defender.setBlocking(true);
        defender.addBlockingTarget(0);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Attacker deals 2 combat damage to defender (4/6)
        // Combat damage is NOT prevented by Magebane Armor
        assertThat(defender.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Noncombat damage prevention does not apply to unequipped creature")
    void noncombatDamageNotPreventedOnUnequippedCreature() {
        Permanent creature = addReadyCreature(player1);
        addArmorReady(player1); // Armor on battlefield but not attached

        // Lightning Bolt targets the unequipped creature
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        // 2/2 creature takes 3 damage and dies
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Re-equip =====

    @Test
    @DisplayName("Moving Magebane Armor transfers all effects to new creature")
    void reEquipTransfersEffects() {
        Permanent armor = addArmorReady(player1);
        Permanent creature1 = addReadyFlyingCreature(player1);
        Permanent creature2 = addReadyCreature(player1);
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

        // creature2 (Grizzly Bears 2/2) gets boost (no flying to lose)
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(4);   // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature2)).isEqualTo(6); // 2 + 4
    }

    // ===== Helpers =====

    private Permanent addArmorReady(Player player) {
        Permanent perm = new Permanent(new MagebaneArmor());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyFlyingCreature(Player player) {
        Permanent perm = new Permanent(new AzureDrake());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
