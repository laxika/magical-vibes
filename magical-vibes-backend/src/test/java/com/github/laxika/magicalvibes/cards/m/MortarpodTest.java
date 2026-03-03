package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MortarpodTest extends BaseCardTest {

    // ===== Living weapon ETB =====

    @Test
    @DisplayName("Casting Mortarpod triggers living weapon ETB on the stack")
    void castingTriggersLivingWeapon() {
        harness.setHand(player1, List.of(new Mortarpod()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        // After the artifact resolves, the living weapon ETB should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry etb = gd.stack.getFirst();
        assertThat(etb.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(etb.getCard().getName()).isEqualTo("Mortarpod");
    }

    @Test
    @DisplayName("Resolving living weapon creates a Phyrexian Germ token and attaches Mortarpod")
    void livingWeaponCreatesGermAndAttaches() {
        harness.setHand(player1, List.of(new Mortarpod()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // Resolve artifact spell
        harness.passBothPriorities(); // Resolve living weapon ETB trigger

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());

        Permanent mortarpod = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Mortarpod"))
                .findFirst().orElseThrow();
        Permanent germ = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Germ"))
                .findFirst().orElseThrow();

        // Mortarpod should be attached to the Germ token
        assertThat(mortarpod.getAttachedTo()).isEqualTo(germ.getId());
    }

    @Test
    @DisplayName("Phyrexian Germ token has correct properties")
    void germTokenHasCorrectProperties() {
        harness.setHand(player1, List.of(new Mortarpod()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Germ"))
                .findFirst().orElseThrow();

        assertThat(germ.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(germ.getCard().getPower()).isEqualTo(0);
        assertThat(germ.getCard().getToughness()).isEqualTo(0);
        assertThat(germ.getCard().isToken()).isTrue();
        assertThat(germ.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.GERM);
    }

    // ===== Germ gets equipment bonuses =====

    @Test
    @DisplayName("Germ token gets +0/+1 from Mortarpod (effective 0/1)")
    void germGetsEquipmentBonus() {
        harness.setHand(player1, List.of(new Mortarpod()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Germ"))
                .findFirst().orElseThrow();

        // 0/0 base + 0/1 from equipment = 0/1 effective
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(1);
    }

    // ===== Granted sacrifice ability: deal 1 damage to creature =====

    @Test
    @DisplayName("Equipped creature can sacrifice itself to deal 1 damage to target creature")
    void grantedAbilityDeals1DamageToCreature() {
        Permanent creature = addReadyCreature(player1);
        Permanent mortarpod = addMortarpodReady(player1);
        mortarpod.setAttachedTo(creature.getId());

        // Target creature on opponent's side (Grizzly Bears: 2/2)
        Permanent targetCreature = addReadyCreature(player2);

        // Activate the granted sacrifice ability (ability index 0 on the creature)
        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        // Target creature should have taken 1 damage (2/2 -> 2/1 effectively, still alive)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(targetCreature.getId()));

        // The equipped creature should be sacrificed (gone from battlefield)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
    }

    // ===== Granted sacrifice ability: deal 1 damage to player =====

    @Test
    @DisplayName("Equipped creature can sacrifice itself to deal 1 damage to a player")
    void grantedAbilityDeals1DamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player1);
        Permanent mortarpod = addMortarpodReady(player1);
        mortarpod.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        // The equipped creature should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
    }

    // ===== Germ can sacrifice itself =====

    @Test
    @DisplayName("Germ token from living weapon can sacrifice itself to deal 1 damage")
    void germCanSacrificeItselfToDealDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Mortarpod()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // Resolve artifact spell
        harness.passBothPriorities(); // Resolve living weapon ETB trigger

        Permanent germ = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Germ"))
                .findFirst().orElseThrow();

        // Germ has summoning sickness, but the sacrifice ability doesn't require tapping
        germ.setSummoningSick(false);

        // Find the germ's index on the battlefield
        int germIndex = gd.playerBattlefields.get(player1.getId()).indexOf(germ);

        harness.activateAbility(player1, germIndex, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        // Germ should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Phyrexian Germ"));

        // Mortarpod should still be on the battlefield (unattached)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mortarpod"));
    }

    // ===== Equip to another creature =====

    @Test
    @DisplayName("Equipping Mortarpod to another creature moves it from the Germ")
    void equipToAnotherCreature() {
        harness.setHand(player1, List.of(new Mortarpod()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Add a creature to equip to
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Equip to bears
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent mortarpod = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mortarpod"))
                .findFirst().orElseThrow();

        assertThat(mortarpod.getAttachedTo()).isEqualTo(bears.getId());

        // Bears should get +0/+1
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);  // 2 + 0
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);  // 2 + 1
    }

    // ===== Germ dies when equipment is moved =====

    @Test
    @DisplayName("Germ token dies (0 toughness) when Mortarpod is moved to another creature")
    void germDiesWhenEquipmentMoved() {
        harness.setHand(player1, List.of(new Mortarpod()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Equip to bears — Germ becomes 0/0 and dies
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        // Germ should be dead (0 toughness, state-based action)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Phyrexian Germ"));
    }

    // ===== Equipment stays when creature is sacrificed =====

    @Test
    @DisplayName("Mortarpod stays on battlefield after equipped creature is sacrificed")
    void equipmentStaysWhenCreatureSacrificed() {
        Permanent creature = addReadyCreature(player1);
        Permanent mortarpod = addMortarpodReady(player1);
        mortarpod.setAttachedTo(creature.getId());

        harness.setLife(player2, 20);

        // Sacrifice creature using granted ability
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Mortarpod should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mortarpod"));
    }

    // ===== Helpers =====

    private Permanent addMortarpodReady(Player player) {
        Permanent perm = new Permanent(new Mortarpod());
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
