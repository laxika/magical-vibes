package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.PreventXDamageFromEachSourceToAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShieldOfTheRealmTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Shield of the Realm has prevent 2 damage from each source effect")
    void hasPreventDamageEffect() {
        ShieldOfTheRealm card = new ShieldOfTheRealm();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(PreventXDamageFromEachSourceToAttachedCreatureEffect.class);
        PreventXDamageFromEachSourceToAttachedCreatureEffect effect =
                (PreventXDamageFromEachSourceToAttachedCreatureEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.amount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Shield of the Realm has equip {1} ability")
    void hasEquipAbility() {
        ShieldOfTheRealm card = new ShieldOfTheRealm();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.getManaCost()).isEqualTo("{1}");
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getTargetFilter()).isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(ability.getTimingRestriction()).isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(ability.getEffects()).singleElement().isInstanceOf(EquipEffect.class);
    }

    // ===== Equip =====

    @Test
    @DisplayName("Resolving equip attaches Shield of the Realm to target creature")
    void resolvingEquipAttaches() {
        Permanent shield = addShieldReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    // ===== Damage prevention from spells =====

    @Test
    @DisplayName("Prevents 2 of 3 noncombat damage to equipped creature")
    void prevents2Of3NoncombatDamage() {
        Permanent creature = addReadyCreature(player1);
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        // Lightning Bolt deals 3 damage to the equipped creature
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        // 3 - 2 prevented = 1 damage taken; creature (2/2) survives
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents all damage when source deals 2 or less")
    void preventsAllDamageWhenSourceDeals2OrLess() {
        Permanent creature = addReadyCreature(player1);
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        // Shock deals 2 damage to the equipped creature
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        // 2 - 2 prevented = 0 damage
        assertThat(creature.getMarkedDamage()).isEqualTo(0);
    }

    // ===== Damage prevention from combat =====

    @Test
    @DisplayName("Prevents 2 combat damage to equipped creature from each attacker")
    void prevents2CombatDamageFromEachSource() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Player1 has a creature equipped with Shield of the Realm
        Permanent defender = addReadyCreature(player1);
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(defender.getId());

        // Player2 attacks with a 2/2
        Permanent attacker = addReadyCreature(player2);
        attacker.setAttacking(true);

        // Defender blocks
        defender.setBlocking(true);
        defender.addBlockingTarget(0);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Attacker deals 2 combat damage - 2 prevented = 0 damage
        assertThat(defender.getMarkedDamage()).isEqualTo(0);
    }

    // ===== No prevention for unequipped creature =====

    @Test
    @DisplayName("Does not prevent damage to unequipped creature")
    void doesNotPreventDamageToUnequippedCreature() {
        Permanent creature = addReadyCreature(player1);
        addShieldReady(player1); // Shield on battlefield but not attached

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        // 2/2 creature takes full 3 damage and dies
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Two shields stack =====

    @Test
    @DisplayName("Two Shields of the Realm prevent 4 damage total per source")
    void twoShieldsPrevent4Damage() {
        Permanent creature = addReadyCreature(player1);
        Permanent shield1 = addShieldReady(player1);
        Permanent shield2 = addShieldReady(player1);
        shield1.setAttachedTo(creature.getId());
        shield2.setAttachedTo(creature.getId());

        // Lightning Bolt deals 3 damage to the equipped creature
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        // 3 - 4 prevented = 0 damage (clamped at 0)
        assertThat(creature.getMarkedDamage()).isEqualTo(0);
    }

    // ===== Re-equip =====

    @Test
    @DisplayName("Moving Shield of the Realm transfers prevention to new creature")
    void reEquipTransfersPrevention() {
        Permanent shield = addShieldReady(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);
        shield.setAttachedTo(creature1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(creature2.getId());

        // Now Lightning Bolt creature2 — should be prevented
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature2.getId());
        harness.passBothPriorities();

        // 3 - 2 = 1 damage to creature2
        assertThat(creature2.getMarkedDamage()).isEqualTo(1);
    }

    // ===== Helpers =====

    private Permanent addShieldReady(Player player) {
        Permanent perm = new Permanent(new ShieldOfTheRealm());
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
}
