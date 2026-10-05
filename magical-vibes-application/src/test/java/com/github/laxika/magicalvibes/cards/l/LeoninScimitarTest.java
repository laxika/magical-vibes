package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeoninScimitar.class, Deathmark.class, GrizzlyBears.class})
class LeoninScimitarTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Leonin Scimitar puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Leonin Scimitar");
    }

    @Test
    @DisplayName("Resolving Leonin Scimitar puts it on the battlefield unattached")
    void resolvingPutsOnBattlefieldUnattached() {
        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leonin Scimitar")
                        && !p.isAttached());
    }

    @Test
    @DisplayName("Activating equip ability puts it on the stack")
    void activatingEquipPutsOnStack() {
        addScimitarReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Leonin Scimitar");
        assertThat(entry.getTargetId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Mana is consumed when activating equip ability")
    void manaConsumedOnEquip() {
        addScimitarReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Equipment is not tapped when equip ability is activated")
    void equipDoesNotTapEquipment() {
        Permanent scimitar = addScimitarReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(scimitar.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Resolving equip ability attaches equipment to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent scimitar = addScimitarReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scimitar = addScimitarReady(player1);
        scimitar.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped creature loses boost when equipment is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scimitar = addScimitarReady(player1);
        scimitar.setAttachedTo(creature.getId());

        // Verify boost is active
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        // Remove equipment
        gd.playerBattlefields.get(player1.getId()).remove(scimitar);

        // Verify boost is gone
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipment does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scimitar = addScimitarReady(player1);
        scimitar.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipment can be moved to another creature by equipping again")
    void canReEquipToAnotherCreature() {
        Permanent scimitar = addScimitarReady(player1);
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player1, new GrizzlyBears());

        // Equip to creature1
        scimitar.setAttachedTo(creature1.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(3);

        // Re-equip to creature2
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipment stays on battlefield unattached when equipped creature is destroyed")
    void equipmentStaysWhenCreatureDies() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scimitar = addScimitarReady(player1);
        scimitar.setAttachedTo(creature.getId());

        // Opponent destroys the equipped creature with Deathmark (targets green creature)
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player2, 0, creature.getId());

        // Creature should be in graveyard
        harness.assertInGraveyard(player1, "Grizzly Bears");
        // Equipment should still be on the battlefield, unattached
        harness.assertOnBattlefield(player1, "Leonin Scimitar");
        assertThat(scimitar.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Cannot equip during opponent's turn")
    void cannotEquipDuringOpponentTurn() {
        addScimitarReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Cannot equip during combat")
    void cannotEquipDuringCombat() {
        addScimitarReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("Cannot equip during upkeep")
    void cannotEquipDuringUpkeep() {
        addScimitarReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("Cannot equip a creature controlled by an opponent")
    void cannotEquipOpponentsCreature() {
        addScimitarReady(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot equip when the stack is not empty")
    void cannotEquipWhenStackNotEmpty() {
        addScimitarReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
    }

    @Test
    @DisplayName("Cannot equip without enough mana")
    void cannotEquipWithoutMana() {
        addScimitarReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Equip fizzles if target creature is removed before resolution")
    void equipFizzlesIfTargetRemoved() {
        addScimitarReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());

        // Remove target creature before resolution
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        // Equipment should still be on battlefield, unattached
        assertThat(gd.stack).isEmpty();
        Permanent scimitar = findPermanent(player1, "Leonin Scimitar");
        assertThat(scimitar.getAttachedTo()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Equip grants the boost only after the ability resolves")
    void equipBoostBeginsOnResolution() {
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(scimitar.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Tapped equipment can equip and still grants its boost")
    void tappedEquipmentCanEquip() {
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        scimitar.setTapped(true);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(scimitar.isTapped()).isTrue();
        assertThat(scimitar.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Failed re-equip leaves the equipment and boost on the original creature")
    void failedReEquipPreservesOriginalAttachment() {
        Permanent scimitar = addScimitarReady(player1);
        Permanent original = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        scimitar.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(scimitar.getAttachedTo()).isEqualTo(original.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(scimitar.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipping the same creature again does not stack the boost")
    void equippingSameCreatureDoesNotStackBoost() {
        Permanent scimitar = addScimitarReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        scimitar.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private Permanent addScimitarReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new LeoninScimitar());
        perm.setSummoningSick(false);
        return perm;
    }
}

