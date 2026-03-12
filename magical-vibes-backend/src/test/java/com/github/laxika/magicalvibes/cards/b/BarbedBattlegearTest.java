package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BarbedBattlegearTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Barbed Battlegear has static +4/-1 boost effect")
    void hasStaticBoostEffect() {
        BarbedBattlegear card = new BarbedBattlegear();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof BoostAttachedCreatureEffect)
                .hasSize(1);
        BoostAttachedCreatureEffect boost = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof BoostAttachedCreatureEffect)
                .map(e -> (BoostAttachedCreatureEffect) e)
                .findFirst().orElseThrow();
        assertThat(boost.powerBoost()).isEqualTo(4);
        assertThat(boost.toughnessBoost()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Barbed Battlegear has equip {2} ability with correct properties")
    void hasEquipAbility() {
        BarbedBattlegear card = new BarbedBattlegear();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{2}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getTargetFilter())
                .isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(card.getActivatedAbilities().get(0).getTimingRestriction())
                .isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(EquipEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Barbed Battlegear for {3} and resolving puts it on the battlefield unattached")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new BarbedBattlegear()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Barbed Battlegear")
                        && !p.isAttached());
    }

    // ===== Equip ability: resolving =====

    @Test
    @DisplayName("Resolving equip ability attaches Battlegear to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent gear = addGearReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gear.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    // ===== Static effects: power/toughness boost =====

    @Test
    @DisplayName("Equipped creature gets +4/-1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent gear = addGearReady(player1);
        gear.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);    // 2 + 4
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1); // 2 - 1
    }

    @Test
    @DisplayName("Equipped creature loses boost when Battlegear is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent gear = addGearReady(player1);
        gear.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);

        gd.playerBattlefields.get(player1.getId()).remove(gear);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Battlegear does not affect unequipped creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addReadyCreature(player1);
        Permanent otherCreature = addReadyCreature(player1);
        Permanent gear = addGearReady(player1);
        gear.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    // ===== Re-equip =====

    @Test
    @DisplayName("Battlegear can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent gear = addGearReady(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);

        gear.setAttachedTo(creature1.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(6);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(gear.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature1)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature2)).isEqualTo(1);
    }

    // ===== Equip fizzle =====

    @Test
    @DisplayName("Equip fizzles if target creature is removed before resolution")
    void equipFizzlesIfTargetRemoved() {
        Permanent gear = addGearReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent remaining = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Barbed Battlegear"))
                .findFirst().orElseThrow();
        assertThat(remaining.getAttachedTo()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Helpers =====

    private Permanent addGearReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = new Permanent(new BarbedBattlegear());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
