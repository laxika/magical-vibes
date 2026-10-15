package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ExpeditionChampion;
import com.github.laxika.magicalvibes.cards.e.ExpeditionHealer;
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

@CardUsed({RelicAxe.class, ExpeditionChampion.class, ExpeditionHealer.class})
class RelicAxeTest extends BaseCardTest {

    @Test
    @DisplayName("Entry trigger attaches to a creature you control")
    void entersAttachedToTargetCreature() {
        Permanent creature = addCreatureReady(player1, new ExpeditionHealer());
        harness.setHand(player1, List.of(new RelicAxe()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        Permanent equipment = findPermanent(player1, "Relic Axe");
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped non-Warrior creature gets +1/+1")
    void equippedNonWarriorGetsBaseBoost() {
        Permanent creature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new RelicAxe());
        equipment.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped Warrior creature gets +2/+1")
    void equippedWarriorGetsWarriorBoost() {
        Permanent creature = addCreatureReady(player1, new ExpeditionChampion());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new RelicAxe());
        equipment.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equip attaches Relic Axe to another creature you control")
    void equipAttachesToTargetCreature() {
        Permanent firstCreature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent secondCreature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new RelicAxe());
        equipment.setAttachedTo(firstCreature.getId());
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
        harness.setHand(player1, List.of(new RelicAxe()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownCreature.getId());
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Relic Axe").getAttachedTo()).isEqualTo(ownCreature.getId());
    }

    @Test
    void canEnterWithoutAControlledCreature() {
        addCreatureReady(player2, new ExpeditionHealer());
        harness.setHand(player1, List.of(new RelicAxe()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Relic Axe").getAttachedTo()).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void enteringWithoutBeingCastAttachesAfterTriggerResolves() {
        Permanent creature = addCreatureReady(player1, new ExpeditionChampion());
        Permanent axe = harness.enterBattlefieldAndReturn(player1, new RelicAxe());

        harness.handlePermanentChosen(player1, creature.getId());
        assertThat(axe.getAttachedTo()).isNull();
        resolveAllTriggers();

        assertThat(axe.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void entryTriggerDoesNotAttachWhenTargetLeaves() {
        Permanent creature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent axe = harness.enterBattlefieldAndReturn(player1, new RelicAxe());
        harness.handlePermanentChosen(player1, creature.getId());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        resolveAllTriggers();

        assertThat(axe.getAttachedTo()).isNull();
    }

    @Test
    void equipTransfersBoostFromWarriorToNonWarrior() {
        Permanent warrior = addCreatureReady(player1, new ExpeditionChampion());
        Permanent cleric = addCreatureReady(player1, new ExpeditionHealer());
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new RelicAxe());
        axe.setAttachedTo(warrior.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 2, null, cleric.getId());
        resolveAllTriggers();

        assertThat(axe.getAttachedTo()).isEqualTo(cleric.getId());
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, cleric)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cleric)).isEqualTo(3);
    }

    @Test
    void failedEquipKeepsOriginalAttachment() {
        Permanent original = addCreatureReady(player1, new ExpeditionChampion());
        Permanent target = addCreatureReady(player1, new ExpeditionHealer());
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new RelicAxe());
        axe.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 2, null, target.getId());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        resolveAllTriggers();

        assertThat(axe.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(4);
    }

    @Test
    void equipCannotTargetOpponentCreature() {
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new RelicAxe());
        Permanent opponent = addCreatureReady(player2, new ExpeditionHealer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(axe.getAttachedTo()).isNull();
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new RelicAxe());
        Permanent creature = addCreatureReady(player1, new ExpeditionHealer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(axe.getAttachedTo()).isNull();
    }

    @Test
    void oldEntryTriggerCannotAttachAxeThatLeftAndReturned() {
        Permanent firstCreature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent secondCreature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent originalAxe = harness.enterBattlefieldAndReturn(player1, new RelicAxe());
        harness.handlePermanentChosen(player1, firstCreature.getId());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, originalAxe));
        harness.setGraveyard(player1, List.of());
        Permanent returnedAxe = harness.enterBattlefieldAndReturn(player1, originalAxe.getCard());
        harness.handlePermanentChosen(player1, secondCreature.getId());
        resolveAllTriggers();

        assertThat(returnedAxe.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(3);
    }

    @Test
    void equipRequiresTwoMana() {
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new RelicAxe());
        Permanent creature = addCreatureReady(player1, new ExpeditionHealer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(axe.getAttachedTo()).isNull();
    }
}
