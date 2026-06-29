package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArgentumArmorTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Argentum Armor has static +6/+6 boost effect")
    void hasStaticBoostEffect() {
        ArgentumArmor card = new ArgentumArmor();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof StaticBoostEffect)
                .hasSize(1);
        StaticBoostEffect boost = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof StaticBoostEffect)
                .map(e -> (StaticBoostEffect) e)
                .findFirst().orElseThrow();
        assertThat(boost.powerBoost()).isEqualTo(6);
        assertThat(boost.toughnessBoost()).isEqualTo(6);
    }

    @Test
    @DisplayName("Argentum Armor has ON_ATTACK destroy target permanent effect")
    void hasOnAttackDestroyEffect() {
        ArgentumArmor card = new ArgentumArmor();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOf(DestroyTargetPermanentEffect.class);
    }

    @Test
    @DisplayName("Argentum Armor has equip {6} ability with correct properties")
    void hasEquipAbility() {
        ArgentumArmor card = new ArgentumArmor();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{6}");
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

    // ===== Static effects: power/toughness boost =====

    @Test
    @DisplayName("Equipped creature gets +6/+6")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);   // 2 + 6
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8); // 2 + 6
    }

    @Test
    @DisplayName("Equipped creature loses boost when Argentum Armor is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);

        gd.playerBattlefields.get(player1.getId()).remove(armor);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    // ===== Attack trigger: target selection =====

    @Test
    @DisplayName("Attacking with equipped creature queues targeted attack trigger for target selection")
    void attackTriggerQueuesForTargetSelection() {
        Permanent creature = addReadyCreature(player1);
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());
        Permanent opponentCreature = addReadyCreature(player2);

        declareAttackers(player1, List.of(0));

        // The trigger should prompt for target selection (permanent choice)
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Choosing target puts triggered ability on the stack")
    void choosingTargetPutsAbilityOnStack() {
        Permanent creature = addReadyCreature(player1);
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());
        Permanent opponentCreature = addReadyCreature(player2);

        declareAttackers(player1, List.of(0));

        // Choose the opponent's creature as target
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        // Triggered ability should be on the stack with the chosen target
        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Argentum Armor")
                        && se.getTargetId().equals(opponentCreature.getId())
                        && se.getSourcePermanentId().equals(armor.getId()));
    }

    @Test
    @DisplayName("Resolving attack trigger destroys the chosen permanent")
    void resolvingTriggerDestroysChosenPermanent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player1);
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());
        Permanent opponentCreature = addReadyCreature(player2);

        declareAttackers(player1, List.of(0));

        // Choose the opponent's creature as target
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Opponent's creature should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(opponentCreature.getId()));
    }

    // ===== Attack trigger: targets any permanent =====

    @Test
    @DisplayName("Attack trigger can target a non-creature permanent (e.g. artifact)")
    void attackTriggerCanTargetAnyPermanent() {
        Permanent creature = addReadyCreature(player1);
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        // Put an artifact on the opponent's battlefield
        Permanent opponentArtifact = addArmorReady(player2);

        declareAttackers(player1, List.of(0));

        // Choose the opponent's artifact as target
        harness.handlePermanentChosen(player1, opponentArtifact.getId());
        harness.passBothPriorities();

        // Opponent's artifact should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(opponentArtifact.getId()));
    }

    // ===== No trigger for unequipped creature =====

    @Test
    @DisplayName("Trigger does not fire when an unequipped creature attacks")
    void noTriggerWhenUnequippedCreatureAttacks() {
        Permanent creature = addReadyCreature(player1);
        addArmorReady(player1); // Armor on battlefield but not attached

        declareAttackers(player1, List.of(0));

        // No targeted trigger should be queued
        assertThat(gd.pendingAttackTriggerTargets).isEmpty();
        // No permanent choice should be requested
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    // ===== Equip ability =====

    @Test
    @DisplayName("Resolving equip ability attaches Argentum Armor to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent armor = addArmorReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private Permanent addArmorReady(Player player) {
        Permanent perm = new Permanent(new ArgentumArmor());
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

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
