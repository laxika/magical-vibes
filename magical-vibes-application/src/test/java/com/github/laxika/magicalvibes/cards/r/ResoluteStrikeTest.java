package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CliffhavenSellSword;
import com.github.laxika.magicalvibes.cards.e.ExpeditionHealer;
import com.github.laxika.magicalvibes.cards.u.UtilityKnife;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ResoluteStrike.class, CliffhavenSellSword.class, ExpeditionHealer.class, UtilityKnife.class})
class ResoluteStrikeTest extends BaseCardTest {

    @Test
    void boostsWarriorAndMayAttachControlledEquipment() {
        Permanent target = addCreatureReady(player2, new CliffhavenSellSword());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new UtilityKnife());

        cast(target);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(equipment.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void decliningEquipmentAttachmentStillAppliesBoost() {
        Permanent target = addCreatureReady(player2, new CliffhavenSellSword());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new UtilityKnife());

        cast(target);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    void nonWarriorDoesNotGetEquipmentChoice() {
        Permanent target = addCreatureReady(player2, new ExpeditionHealer());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new UtilityKnife());

        cast(target);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    void selectedEquipmentRemainsAttachedAfterEndOfTurn() {
        Permanent target = addCreatureReady(player2, new CliffhavenSellSword());
        Permanent firstEquipment = harness.addToBattlefieldAndReturn(player1, new UtilityKnife());
        Permanent secondEquipment = harness.addToBattlefieldAndReturn(player1, new UtilityKnife());

        cast(target);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, secondEquipment.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(firstEquipment.getAttachedTo()).isNull();
        assertThat(secondEquipment.getAttachedTo()).isEqualTo(target.getId());
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new UtilityKnife());
        harness.setHand(player1, List.of(new ResoluteStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, equipment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void cannotAttachEquipmentControlledByOpponent() {
        Permanent target = addCreatureReady(player2, new CliffhavenSellSword());
        Permanent ownEquipment = harness.addToBattlefieldAndReturn(player1, new UtilityKnife());
        Permanent opposingEquipment = harness.addToBattlefieldAndReturn(player2, new UtilityKnife());

        cast(target);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ownEquipment.getAttachedTo()).isEqualTo(target.getId());
        assertThat(opposingEquipment.getAttachedTo()).isNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void movesAlreadyAttachedEquipmentToWarrior() {
        Permanent previousHost = addCreatureReady(player1, new ExpeditionHealer());
        Permanent target = addCreatureReady(player2, new CliffhavenSellSword());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new UtilityKnife());
        equipment.setAttachedTo(previousHost.getId());

        cast(target);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(equipment.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, previousHost)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void boostsWarriorEvenWithoutEquipment() {
        Permanent target = addCreatureReady(player1, new CliffhavenSellSword());

        cast(target);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removedTargetPreventsEquipmentAttachment() {
        Permanent target = addCreatureReady(player2, new CliffhavenSellSword());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new UtilityKnife());
        harness.setHand(player1, List.of(new ResoluteStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new ResoluteStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
