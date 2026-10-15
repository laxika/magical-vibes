package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ExpeditionHealer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyclavePickAxe.class, ExpeditionHealer.class, Forest.class})
class SkyclavePickAxeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached to a creature you control")
    void entersAttachedToTargetCreature() {
        Permanent creature = addCreatureReady(player1, new ExpeditionHealer());
        harness.setHand(player1, List.of(new SkyclavePickAxe()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        Permanent equipment = findPermanent(player1, "Skyclave Pick-Axe");
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Landfall gives the equipped creature +2/+2 until end of turn")
    void landfallBoostsEquippedCreatureUntilEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SkyclavePickAxe());
        equipment.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip attaches Skyclave Pick-Axe to another creature")
    void equipAttachesToTargetCreature() {
        Permanent firstCreature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent secondCreature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SkyclavePickAxe());
        equipment.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 2, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(secondCreature.getId());
    }

    @Test
    @DisplayName("Cannot target an opponent's creature when entering")
    void cannotTargetOpponentsCreature() {
        Permanent ownCreature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent opponentCreature = addCreatureReady(player2, new ExpeditionHealer());
        harness.setHand(player1, List.of(new SkyclavePickAxe()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownCreature.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Skyclave Pick-Axe").getAttachedTo())
                .isEqualTo(ownCreature.getId());
    }

    @Test
    void canEnterWithoutAControlledCreature() {
        addCreatureReady(player2, new ExpeditionHealer());
        harness.setHand(player1, List.of(new SkyclavePickAxe()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Skyclave Pick-Axe").getAttachedTo()).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void entryTriggerDoesNotAttachWhenTargetLeaves() {
        Permanent creature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent equipment = harness.enterBattlefieldAndReturn(player1, new SkyclavePickAxe());
        harness.handlePermanentChosen(player1, creature.getId());
        assertThat(equipment.getAttachedTo()).isNull();

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        resolveAllTriggers();

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
    }

    @Test
    void landfallUsesAttachmentAtResolution() {
        Permanent firstCreature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent secondCreature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SkyclavePickAxe());
        equipment.setAttachedTo(firstCreature.getId());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        equipment.setAttachedTo(secondCreature.getId());
        resolveAllTriggers();

        assertThat(firstCreature.getEffectivePower()).isEqualTo(2);
        assertThat(firstCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(secondCreature.getEffectivePower()).isEqualTo(4);
        assertThat(secondCreature.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void resolvedBonusStaysWithCreatureAfterReequip() {
        Permanent firstCreature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent secondCreature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SkyclavePickAxe());
        equipment.setAttachedTo(firstCreature.getId());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 2, null, secondCreature.getId());
        resolveAllTriggers();

        assertThat(equipment.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(firstCreature.getEffectivePower()).isEqualTo(4);
        assertThat(firstCreature.getEffectiveToughness()).isEqualTo(4);
        assertThat(secondCreature.getEffectivePower()).isEqualTo(2);
        assertThat(secondCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void landfallDoesNothingWhenUnattachedAtResolution() {
        Permanent creature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SkyclavePickAxe());
        equipment.setAttachedTo(creature.getId());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        equipment.setAttachedTo(null);
        resolveAllTriggers();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void opponentLandDoesNotTriggerLandfall() {
        Permanent creature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SkyclavePickAxe());
        equipment.setAttachedTo(creature.getId());

        harness.enterBattlefieldAndReturn(player2, new Forest());
        assertThat(gd.stack).isEmpty();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void multipleLandEntriesGiveCumulativeBonuses() {
        Permanent creature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SkyclavePickAxe());
        equipment.setAttachedTo(creature.getId());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        resolveAllTriggers();

        assertThat(creature.getEffectivePower()).isEqualTo(6);
        assertThat(creature.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    void equipCannotBeActivatedWithLandfallOnStack() {
        Permanent creature = addCreatureReady(player1, new ExpeditionHealer());
        harness.addToBattlefieldAndReturn(player1, new SkyclavePickAxe());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        resolveAllTriggers();
    }
    @Test
    void landfallUsesLastKnownAttachmentWhenEquipmentLeaves() {
        Permanent creature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SkyclavePickAxe());
        equipment.setAttachedTo(creature.getId());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, equipment));
        resolveAllTriggers();

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new ExpeditionHealer());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SkyclavePickAxe());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    void equipRequiresGreenMana() {
        Permanent creature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SkyclavePickAxe());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(equipment.getAttachedTo()).isNull();
    }
}
