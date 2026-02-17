package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.filter.ControllerOnlyTargetFilter;
import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeoninScimitarTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Leonin Scimitar has correct card properties")
    void hasCorrectProperties() {
        LeoninScimitar card = new LeoninScimitar();

        assertThat(card.getName()).isEqualTo("Leonin Scimitar");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{1}");
        assertThat(card.getColor()).isNull();
        assertThat(card.getSubtypes()).contains(CardSubtype.EQUIPMENT);
    }

    @Test
    @DisplayName("Leonin Scimitar has static +1/+1 boost effect")
    void hasStaticBoostEffect() {
        LeoninScimitar card = new LeoninScimitar();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(BoostEquippedCreatureEffect.class);
        BoostEquippedCreatureEffect boost = (BoostEquippedCreatureEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(1);
    }

    @Test
    @DisplayName("Leonin Scimitar has equip ability with correct properties")
    void hasEquipAbility() {
        LeoninScimitar card = new LeoninScimitar();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getTargetFilter())
                .isInstanceOf(ControllerOnlyTargetFilter.class);
        assertThat(card.getActivatedAbilities().get(0).getTimingRestriction())
                .isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(EquipEffect.class);
    }

    // ===== Casting =====

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
                        && p.getAttachedTo() == null);
    }

    // ===== Equip ability: activating =====

    @Test
    @DisplayName("Activating equip ability puts it on the stack")
    void activatingEquipPutsOnStack() {
        Permanent scimitar = addScimitarReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Leonin Scimitar");
        assertThat(entry.getTargetPermanentId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Mana is consumed when activating equip ability")
    void manaConsumedOnEquip() {
        addScimitarReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Equipment is not tapped when equip ability is activated")
    void equipDoesNotTapEquipment() {
        Permanent scimitar = addScimitarReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(scimitar.isTapped()).isFalse();
    }

    // ===== Equip ability: resolving =====

    @Test
    @DisplayName("Resolving equip ability attaches equipment to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent scimitar = addScimitarReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent scimitar = addScimitarReady(player1);
        scimitar.setAttachedTo(creature.getId());

        assertThat(gs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped creature loses boost when equipment is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent scimitar = addScimitarReady(player1);
        scimitar.setAttachedTo(creature.getId());

        // Verify boost is active
        assertThat(gs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        // Remove equipment
        gd.playerBattlefields.get(player1.getId()).remove(scimitar);

        // Verify boost is gone
        assertThat(gs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipment does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addReadyCreature(player1);
        Permanent otherCreature = addReadyCreature(player1);
        Permanent scimitar = addScimitarReady(player1);
        scimitar.setAttachedTo(creature.getId());

        assertThat(gs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    // ===== Re-equip: moving equipment to another creature =====

    @Test
    @DisplayName("Equipment can be moved to another creature by equipping again")
    void canReEquipToAnotherCreature() {
        Permanent scimitar = addScimitarReady(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);

        // Equip to creature1
        scimitar.setAttachedTo(creature1.getId());
        assertThat(gs.getEffectivePower(gd, creature1)).isEqualTo(3);

        // Re-equip to creature2
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gs.getEffectivePower(gd, creature2)).isEqualTo(3);
    }

    // ===== Equipment stays when creature dies =====

    @Test
    @DisplayName("Equipment stays on battlefield unattached when equipped creature is destroyed")
    void equipmentStaysWhenCreatureDies() {
        Permanent creature = addReadyCreature(player1);
        Permanent scimitar = addScimitarReady(player1);
        scimitar.setAttachedTo(creature.getId());

        // Opponent destroys the equipped creature with Deathmark (targets green creature)
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castSorcery(player2, 0, creature.getId());
        harness.passBothPriorities();

        // Creature should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Equipment should still be on the battlefield, unattached
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
        assertThat(scimitar.getAttachedTo()).isNull();
    }

    // ===== Sorcery-speed timing restriction =====

    @Test
    @DisplayName("Cannot equip during opponent's turn")
    void cannotEquipDuringOpponentTurn() {
        addScimitarReady(player1);
        Permanent creature = addReadyCreature(player1);
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
        Permanent creature = addReadyCreature(player1);
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
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("Cannot equip when the stack is not empty")
    void cannotEquipWhenStackNotEmpty() {
        addScimitarReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        // Put something on the stack
        gd.stack.add(new StackEntry(
                StackEntryType.ACTIVATED_ABILITY,
                new GrizzlyBears(),
                player2.getId(),
                "dummy ability",
                List.of()
        ));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
    }

    @Test
    @DisplayName("Cannot equip without enough mana")
    void cannotEquipWithoutMana() {
        addScimitarReady(player1);
        Permanent creature = addReadyCreature(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Equip fizzle =====

    @Test
    @DisplayName("Equip fizzles if target creature is removed before resolution")
    void equipFizzlesIfTargetRemoved() {
        addScimitarReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());

        // Remove target creature before resolution
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        // Equipment should still be on battlefield, unattached
        assertThat(gd.stack).isEmpty();
        Permanent scimitar = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Leonin Scimitar"))
                .findFirst().orElseThrow();
        assertThat(scimitar.getAttachedTo()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Helpers =====

    private Permanent addScimitarReady(Player player) {
        Permanent perm = new Permanent(new LeoninScimitar());
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
