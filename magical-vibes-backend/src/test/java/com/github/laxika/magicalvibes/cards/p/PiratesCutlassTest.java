package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DireFleetCaptain;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PiratesCutlassTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Pirate's Cutlass has ETB attach effect")
    void hasEtbAttachEffect() {
        PiratesCutlass card = new PiratesCutlass();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(AttachSourceEquipmentToTargetCreatureEffect.class);
    }

    @Test
    @DisplayName("Pirate's Cutlass has static +2/+1 boost")
    void hasStaticBoost() {
        PiratesCutlass card = new PiratesCutlass();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof StaticBoostEffect)
                .hasSize(1);
        StaticBoostEffect boost = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof StaticBoostEffect)
                .map(e -> (StaticBoostEffect) e)
                .findFirst().orElseThrow();
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(1);
    }

    // ===== ETB attach to Pirate =====

    @Test
    @DisplayName("Casting with a Pirate on the battlefield triggers ETB attach")
    void castingTriggersEtbAttachToPirate() {
        harness.addToBattlefield(player1, new DireFleetCaptain());
        harness.setHand(player1, List.of(new PiratesCutlass()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID pirateId = findPermanent(player1, "Dire Fleet Captain").getId();
        harness.castArtifact(player1, 0, pirateId);
        harness.passBothPriorities();

        // After the artifact resolves, the ETB trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry etb = gd.stack.getFirst();
        assertThat(etb.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(etb.getCard().getName()).isEqualTo("Pirate's Cutlass");
        assertThat(etb.getTargetId()).isEqualTo(pirateId);
    }

    @Test
    @DisplayName("Resolving ETB attaches Pirate's Cutlass to the target Pirate")
    void etbAttachesEquipmentToTargetPirate() {
        harness.addToBattlefield(player1, new DireFleetCaptain());
        harness.setHand(player1, List.of(new PiratesCutlass()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID pirateId = findPermanent(player1, "Dire Fleet Captain").getId();
        harness.castArtifact(player1, 0, pirateId);
        // Resolve artifact spell
        harness.passBothPriorities();
        // Resolve ETB trigger
        harness.passBothPriorities();

        Permanent cutlass = findPermanent(player1, "Pirate's Cutlass");
        assertThat(cutlass.getAttachedTo()).isEqualTo(pirateId);
    }

    @Test
    @DisplayName("Target Pirate gets +2/+1 after ETB attachment")
    void targetPirateGetsBoostAfterAttachment() {
        harness.addToBattlefield(player1, new DireFleetCaptain());
        harness.setHand(player1, List.of(new PiratesCutlass()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID pirateId = findPermanent(player1, "Dire Fleet Captain").getId();
        harness.castArtifact(player1, 0, pirateId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent pirate = findPermanent(player1, "Dire Fleet Captain");
        // Dire Fleet Captain is 2/2 base + 2/1 from equipment = 4/3 effective
        assertThat(gqs.getEffectivePower(gd, pirate)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, pirate)).isEqualTo(3);
    }

    // ===== ETB without target =====

    @Test
    @DisplayName("Can cast without target when no Pirates are controlled")
    void canCastWithoutTargetWhenNoPirates() {
        harness.setHand(player1, List.of(new PiratesCutlass()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pirate's Cutlass");
    }

    @Test
    @DisplayName("ETB does not trigger when cast without a target")
    void etbDoesNotTriggerWithoutTarget() {
        harness.setHand(player1, List.of(new PiratesCutlass()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Pirate's Cutlass"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can cast without target when only non-Pirate creatures are controlled")
    void canCastWithoutTargetWhenOnlyNonPirates() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PiratesCutlass()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Pirate's Cutlass"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== ETB fizzle =====

    @Test
    @DisplayName("ETB fizzles if target Pirate is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new DireFleetCaptain());
        harness.setHand(player1, List.of(new PiratesCutlass()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID pirateId = findPermanent(player1, "Dire Fleet Captain").getId();
        harness.castArtifact(player1, 0, pirateId);
        harness.passBothPriorities();

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Dire Fleet Captain"));

        harness.passBothPriorities();

        // Equipment should be on battlefield, unattached
        Permanent cutlass = findPermanent(player1, "Pirate's Cutlass");
        assertThat(cutlass.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Equip ability =====

    @Test
    @DisplayName("Equip moves Pirate's Cutlass to a different creature")
    void equipMovesEquipmentToAnotherCreature() {
        // Set up: Cutlass attached to pirate via ETB
        harness.addToBattlefield(player1, new DireFleetCaptain());
        harness.setHand(player1, List.of(new PiratesCutlass()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID pirateId = findPermanent(player1, "Dire Fleet Captain").getId();
        harness.castArtifact(player1, 0, pirateId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Add a non-Pirate creature — Equip can target any creature you control
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Equip to bears for {2}
        harness.addMana(player1, ManaColor.WHITE, 2);
        int cutlassIndex = findPermanentIndex(player1, "Pirate's Cutlass");
        harness.activateAbility(player1, cutlassIndex, null, bears.getId());
        harness.passBothPriorities();

        Permanent cutlass = findPermanent(player1, "Pirate's Cutlass");
        assertThat(cutlass.getAttachedTo()).isEqualTo(bears.getId());

        // Bears get +2/+1
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        // Pirate no longer gets bonus
        Permanent pirate = findPermanent(player1, "Dire Fleet Captain");
        assertThat(gqs.getEffectivePower(gd, pirate)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, pirate)).isEqualTo(2);
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
