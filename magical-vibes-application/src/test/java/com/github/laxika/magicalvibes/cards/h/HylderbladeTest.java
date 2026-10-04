package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnightLuminary;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hylderblade.class, GrizzlyBears.class, Forest.class, KnightLuminary.class})
class HylderbladeTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void equipAttachesToCreatureYouControl() {
        Permanent blade = addBladeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void voidAbilityAttachesAtEndStepAfterNonlandPermanentLeaves() {
        Permanent blade = addBladeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));

        advanceToEndStep();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(creature.getId()).doesNotContain(departed.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void voidAbilityDoesNotTriggerWithoutVoidEvent() {
        Permanent blade = addBladeReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(blade.getAttachedTo()).isNull();
    }

    @Test
    void voidAbilityDoesNotTriggerWhenOnlyALandLeaves() {
        Permanent blade = addBladeReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, land));

        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(blade.getAttachedTo()).isNull();
    }

    @Test
    void voidAbilityTriggersAfterWarpWithoutAPermanentLeaving() {
        Permanent blade = addBladeReady(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KnightLuminary());
        harness.setHand(player1, List.of(new KnightLuminary()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        assertThat(gd.nonlandPermanentLeftBattlefieldThisTurn).isFalse();
        advanceToEndStep();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(target.getId()).doesNotContain(blade.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(blade.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void voidAbilityMovesEquipmentAndBoostToTheChosenCreature() {
        Permanent blade = addBladeReady(player1);
        Permanent original = harness.addToBattlefieldAndReturn(player1, new KnightLuminary());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KnightLuminary());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new KnightLuminary());
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new Hylderblade());
        blade.setAttachedTo(original.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));

        advanceToEndStep();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(original.getId(), target.getId())
                .doesNotContain(opponent.getId(), blade.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(blade.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void voidAbilityDoesNotTriggerDuringOpponentsEndStep() {
        Permanent blade = addBladeReady(player1);
        harness.addToBattlefield(player1, new KnightLuminary());
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new Hylderblade());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(blade.getAttachedTo()).isNull();
    }

    @Test
    void voidAbilityCannotAttachWhenChosenCreatureLeavesBeforeResolution() {
        Permanent blade = addBladeReady(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KnightLuminary());
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new Hylderblade());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));
        advanceToEndStep();
        harness.handlePermanentChosen(player1, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        resolveAllTriggers();

        assertThat(blade.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void voidAbilityHasNoLegalTargetWhenOnlyOpponentControlsCreatures() {
        Permanent blade = addBladeReady(player1);
        harness.addToBattlefield(player2, new KnightLuminary());
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new Hylderblade());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));

        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(blade.getAttachedTo()).isNull();
    }

    private Permanent addBladeReady(Player player) {
        return addCreatureReady(player, new Hylderblade());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
