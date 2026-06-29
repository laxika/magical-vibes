package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PistonSledgeTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Piston Sledge has ETB attach effect")
    void hasEtbAttachEffect() {
        PistonSledge card = new PistonSledge();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(AttachSourceEquipmentToTargetCreatureEffect.class);
    }

    @Test
    @DisplayName("Piston Sledge has static +3/+1 boost")
    void hasStaticBoost() {
        PistonSledge card = new PistonSledge();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof StaticBoostEffect)
                .hasSize(1);
        StaticBoostEffect boost = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof StaticBoostEffect)
                .map(e -> (StaticBoostEffect) e)
                .findFirst().orElseThrow();
        assertThat(boost.powerBoost()).isEqualTo(3);
        assertThat(boost.toughnessBoost()).isEqualTo(1);
    }

    @Test
    @DisplayName("Piston Sledge has equip ability with sacrifice artifact cost")
    void hasEquipAbilityWithSacrificeArtifactCost() {
        PistonSledge card = new PistonSledge();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getTargetFilter())
                .isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(card.getActivatedAbilities().get(0).getTimingRestriction())
                .isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(card.getActivatedAbilities().get(0).getEffects())
                .hasSize(2)
                .satisfies(effects -> {
                    assertThat(effects.get(0)).isInstanceOf(SacrificeArtifactCost.class);
                    assertThat(effects.get(1)).isInstanceOf(EquipEffect.class);
                });
    }

    // ===== ETB attach =====

    @Test
    @DisplayName("Casting Piston Sledge with a target creature triggers ETB attach")
    void castingTriggersEtbAttach() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PistonSledge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID bearsId = findPermanent(player1, "Grizzly Bears").getId();
        harness.castArtifact(player1, 0, bearsId);
        harness.passBothPriorities();

        // After the artifact resolves, the ETB trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry etb = gd.stack.getFirst();
        assertThat(etb.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(etb.getCard().getName()).isEqualTo("Piston Sledge");
        assertThat(etb.getTargetId()).isEqualTo(bearsId);
    }

    @Test
    @DisplayName("Resolving ETB attaches Piston Sledge to the target creature")
    void etbAttachesEquipmentToTargetCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PistonSledge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID bearsId = findPermanent(player1, "Grizzly Bears").getId();
        harness.castArtifact(player1, 0, bearsId);
        // Resolve artifact spell
        harness.passBothPriorities();
        // Resolve ETB trigger
        harness.passBothPriorities();

        Permanent pistonSledge = findPermanent(player1, "Piston Sledge");
        assertThat(pistonSledge.getAttachedTo()).isEqualTo(bearsId);
    }

    @Test
    @DisplayName("Target creature gets +3/+1 after ETB attachment")
    void targetCreatureGetsBoostAfterAttachment() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PistonSledge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID bearsId = findPermanent(player1, "Grizzly Bears").getId();
        harness.castArtifact(player1, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        // 2/2 base + 3/1 from equipment = 5/3 effective
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    // ===== ETB without target =====

    @Test
    @DisplayName("Can cast without target when no creatures are controlled")
    void canCastWithoutTargetWhenNoCreatures() {
        harness.setHand(player1, List.of(new PistonSledge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Piston Sledge");
    }

    @Test
    @DisplayName("ETB does not trigger when cast without a target")
    void etbDoesNotTriggerWithoutTarget() {
        harness.setHand(player1, List.of(new PistonSledge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Piston Sledge"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== ETB fizzle =====

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PistonSledge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID bearsId = findPermanent(player1, "Grizzly Bears").getId();
        harness.castArtifact(player1, 0, bearsId);
        harness.passBothPriorities();

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        // Equipment should be on battlefield, unattached
        Permanent pistonSledge = findPermanent(player1, "Piston Sledge");
        assertThat(pistonSledge.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Equip with sacrifice artifact cost =====

    @Test
    @DisplayName("Equip by sacrificing another artifact moves equipment to target creature")
    void equipBySacrificingArtifact() {
        harness.addToBattlefield(player1, new PistonSledge());
        harness.addToBattlefield(player1, new Spellbook());
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        UUID spellbookId = findPermanent(player1, "Spellbook").getId();
        harness.activateAbility(player1, 0, null, bears.getId());
        // Choose Spellbook as the artifact to sacrifice
        harness.handlePermanentChosen(player1, spellbookId);
        harness.passBothPriorities();

        Permanent pistonSledge = findPermanent(player1, "Piston Sledge");
        assertThat(pistonSledge.getAttachedTo()).isEqualTo(bears.getId());

        // Spellbook should have been sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));

        // Bears should get +3/+1
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip moves equipment from one creature to another")
    void equipMovesEquipmentBetweenCreatures() {
        // Set up: Piston Sledge attached to first creature
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PistonSledge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID bearsId = findPermanent(player1, "Grizzly Bears").getId();
        harness.castArtifact(player1, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Add a second creature and another artifact to sacrifice
        Permanent secondCreature = new Permanent(new GrizzlyBears());
        secondCreature.getCard().setName("Second Bear");
        secondCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(secondCreature);
        harness.addToBattlefield(player1, new Spellbook());

        // Equip to second creature by sacrificing Spellbook
        UUID spellbookId = findPermanent(player1, "Spellbook").getId();
        int sledgeIndex = findPermanentIndex(player1, "Piston Sledge");
        harness.activateAbility(player1, sledgeIndex, null, secondCreature.getId());
        // Choose Spellbook as the artifact to sacrifice
        harness.handlePermanentChosen(player1, spellbookId);
        harness.passBothPriorities();

        Permanent pistonSledge = findPermanent(player1, "Piston Sledge");
        assertThat(pistonSledge.getAttachedTo()).isEqualTo(secondCreature.getId());

        // Second creature gets +3/+1
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(3);

        // First creature no longer has bonus
        Permanent firstBear = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, firstBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstBear)).isEqualTo(2);
    }

    // ===== Helpers =====

    private int findPermanentIndex(Player player, String name) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals(name)) {
                return i;
            }
        }
        throw new AssertionError("Permanent not found: " + name);
    }
}
