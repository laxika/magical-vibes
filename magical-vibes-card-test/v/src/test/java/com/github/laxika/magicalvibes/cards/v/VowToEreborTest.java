package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LiberatedDwarf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VowToErebor.class, GrizzlyBears.class, LiberatedDwarf.class, LeoninScimitar.class})
class VowToEreborTest extends BaseCardTest {

    @Test
    void untapsAndBoostsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.tap();
        castVow(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void DwarfMayHaveControlledEquipmentAttached() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LiberatedDwarf());
        target.tap();
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        castVow(target);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(equipment.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void nonDwarfDoesNotOfferEquipmentChoice() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        castVow(target);

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castVow(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void canOnlyTargetCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VowToErebor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void dwarfCanDeclineAttachmentAndStillUntapAndGetBoosted() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LiberatedDwarf());
        target.tap();
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        castVow(target);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void choosesOnlyOneControlledEquipment() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LiberatedDwarf());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        castVow(target);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(first.getAttachedTo()).isNull();
        assertThat(second.getAttachedTo()).isEqualTo(target.getId());
        assertThat(opposing.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void dwarfWithNoControlledEquipmentStillUntapsAndGetsBoosted() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LiberatedDwarf());
        target.tap();
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        castVow(target);
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(opposing.getAttachedTo()).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void attachedEquipmentStaysAttachedAfterEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LiberatedDwarf());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        castVow(target);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(equipment.getAttachedTo()).isEqualTo(target.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(equipment.getAttachedTo()).isEqualTo(target.getId());

        harness.passBothPriorities();
        assertThat(equipment.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    private void castVow(Permanent target) {
        harness.setHand(player1, List.of(new VowToErebor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
