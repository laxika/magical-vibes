package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GlintHawk;
import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArgentumArmor.class, GlintHawk.class, DarksteelMyr.class})
class ArgentumArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Equip requires six mana")
    void equipRequiresSixMana() {
        Permanent armor = addArmorReady(player1);
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(armor.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +6/+6")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);   // 2 + 6
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8); // 2 + 6
    }

    @Test
    @DisplayName("Equipped creature loses boost when Argentum Armor is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);

        gd.playerBattlefields.get(player1.getId()).remove(armor);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking with equipped creature queues targeted attack trigger for target selection")
    void attackTriggerQueuesForTargetSelection() {
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());
        addCreatureReady(player2, new GlintHawk());

        declareAttackers(player1, List.of(0));

        // The trigger should prompt for target selection (permanent choice)
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Choosing target puts triggered ability on the stack")
    void choosingTargetPutsAbilityOnStack() {
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());
        Permanent opponentCreature = addCreatureReady(player2, new GlintHawk());

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

        Permanent creature = addCreatureReady(player1, new GlintHawk());
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());
        Permanent opponentCreature = addCreatureReady(player2, new GlintHawk());

        declareAttackers(player1, List.of(0));

        // Choose the opponent's creature as target
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Opponent's creature should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(opponentCreature.getId()));
    }

    @Test
    @DisplayName("Attack trigger can target a non-creature permanent (e.g. artifact)")
    void attackTriggerCanTargetAnyPermanent() {
        Permanent creature = addCreatureReady(player1, new GlintHawk());
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

    @Test
    @DisplayName("Trigger does not fire when an unequipped creature attacks")
    void noTriggerWhenUnequippedCreatureAttacks() {
        addCreatureReady(player1, new GlintHawk());
        addArmorReady(player1); // Armor on battlefield but not attached

        declareAttackers(player1, List.of(0));

        // No targeted trigger should be queued
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
        // No permanent choice should be requested
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Resolving equip ability attaches Argentum Armor to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent armor = addArmorReady(player1);
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Attack trigger can destroy its own Equipment")
    void attackTriggerCanDestroyArmorItself() {
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, armor.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(armor);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(armor.getCard());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attack trigger still resolves after the Equipment leaves")
    void attackTriggerSurvivesEquipmentRemoval() {
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());
        Permanent target = addArmorReady(player2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(armor);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Equipment controller chooses the target when another player controls the attacker")
    void equipmentControllerControlsAttackTrigger() {
        Permanent creature = addCreatureReady(player2, new GlintHawk());
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        declareAttackers(player2, List.of(0));
        harness.handlePermanentChosen(player1, armor.getId());

        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && armor.getId().equals(entry.getSourcePermanentId())
                        && player1.getId().equals(entry.getControllerId()));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(armor);
    }

    @Test
    @DisplayName("Destroy trigger cannot destroy an indestructible permanent")
    void indestructibleTargetSurvives() {
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(target.getCard());
    }

    @Test
    @DisplayName("Equip cannot target another player's creature or a noncreature")
    void equipRejectsIllegalTargets() {
        Permanent armor = addArmorReady(player1);
        Permanent opposingCreature = addCreatureReady(player2, new GlintHawk());
        harness.addMana(player1, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, armor.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(armor.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void equipRequiresSorceryTiming() {
        Permanent armor = addArmorReady(player1);
        Permanent creature = addCreatureReady(player1, new GlintHawk());
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(armor.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Re-equipping moves the boost to the new creature")
    void reequippingMovesBoost() {
        Permanent armor = addArmorReady(player1);
        Permanent first = addCreatureReady(player1, new GlintHawk());
        Permanent second = addCreatureReady(player1, new GlintHawk());
        armor.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.activateAbility(player1, 0, null, second.getId());
        assertThat(armor.getAttachedTo()).isEqualTo(first.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(8);
    }

    @Test
    @DisplayName("An illegal equip target leaves the old attachment intact")
    void removedEquipTargetDoesNotDetachArmor() {
        Permanent armor = addArmorReady(player1);
        Permanent first = addCreatureReady(player1, new GlintHawk());
        Permanent second = addCreatureReady(player1, new GlintHawk());
        armor.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.activateAbility(player1, 0, null, second.getId());
        gd.playerBattlefields.get(player1.getId()).remove(second);
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(8);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addArmorReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ArgentumArmor());
    }
}
