package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StriderHarness;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrassSquireTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Brass Squire has one activated ability with multi-target")
    void hasMultiTargetActivatedAbility() {
        BrassSquire card = new BrassSquire();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).isMultiTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getMinTargets()).isEqualTo(2);
        assertThat(card.getActivatedAbilities().get(0).getMaxTargets()).isEqualTo(2);
        assertThat(card.getActivatedAbilities().get(0).getMultiTargetFilters()).hasSize(2);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(AttachTargetEquipmentToTargetCreatureEffect.class);
    }

    // ===== Attach equipment to creature (happy path) =====

    @Test
    @DisplayName("Activating ability puts it on the stack with both targets")
    void activatingAbilityPutsOnStack() {
        Permanent squire = addSquireReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        Permanent creature = addReadyCreature(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature.getId()));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Brass Squire");
        assertThat(entry.getTargetIds()).containsExactly(equipment.getId(), creature.getId());
    }

    @Test
    @DisplayName("Resolving ability attaches equipment to target creature")
    void resolvingAbilityAttachesEquipment() {
        Permanent squire = addSquireReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        Permanent creature = addReadyCreature(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    // ===== Move equipment from one creature to another =====

    @Test
    @DisplayName("Can move equipment from one creature to another")
    void canMoveEquipmentBetweenCreatures() {
        Permanent squire = addSquireReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);

        // First attach to creature1
        equipment.setAttachedTo(creature1.getId());
        assertThat(equipment.getAttachedTo()).isEqualTo(creature1.getId());

        // Use Brass Squire to move to creature2
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature2.getId()));
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature2.getId());
    }

    // ===== Fizzle cases =====

    @Test
    @DisplayName("Ability fizzles if equipment leaves battlefield before resolution")
    void fizzlesIfEquipmentLeaves() {
        Permanent squire = addSquireReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        Permanent creature = addReadyCreature(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature.getId()));

        // Remove equipment before resolution
        gd.playerBattlefields.get(player1.getId()).remove(equipment);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // Equipment is gone so nothing should be attached
    }

    @Test
    @DisplayName("Ability fizzles if creature leaves battlefield before resolution")
    void fizzlesIfCreatureLeaves() {
        Permanent squire = addSquireReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        Permanent creature = addReadyCreature(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature.getId()));

        // Remove creature before resolution
        gd.playerBattlefields.get(player1.getId()).remove(creature);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(equipment.getAttachedTo()).isNull();
    }

    // ===== Summoning sickness =====

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent squire = new Permanent(new BrassSquire());
        // Summoning sick by default
        gd.playerBattlefields.get(player1.getId()).add(squire);
        Permanent equipment = addEquipmentReady(player1);
        Permanent creature = addReadyCreature(player1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    // ===== Instant speed =====

    @Test
    @DisplayName("Can activate at instant speed (during opponent's turn)")
    void worksAtInstantSpeed() {
        Permanent squire = addSquireReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        Permanent creature = addReadyCreature(player1);

        // Force to opponent's turn
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Player1 should still be able to activate (instant speed)
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Brass Squire");
    }

    // ===== Taps the squire =====

    @Test
    @DisplayName("Activating the ability taps Brass Squire")
    void activatingTapsSquire() {
        Permanent squire = addSquireReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        Permanent creature = addReadyCreature(player1);

        assertThat(squire.isTapped()).isFalse();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature.getId()));

        assertThat(squire.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent squire = addSquireReady(player1);
        squire.tap();
        Permanent equipment = addEquipmentReady(player1);
        Permanent creature = addReadyCreature(player1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Helpers =====

    private Permanent addSquireReady(Player player) {
        Permanent perm = new Permanent(new BrassSquire());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addEquipmentReady(Player player) {
        Permanent perm = new Permanent(new StriderHarness());
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
