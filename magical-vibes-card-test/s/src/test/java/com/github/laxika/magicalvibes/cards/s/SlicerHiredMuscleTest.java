package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlicerHiredMuscle.class, SlicerHighSpeedAntagonist.class})
class SlicerHiredMuscleTest extends BaseCardTest {

    @Test
    void moreThanMeetsTheEyeCastsSlicerConvertedWithLivingMetal() {
        Permanent slicer = castConvertedSlicer();

        assertThat(slicer.isTransformed()).isTrue();
        assertThat(slicer.getCard()).isInstanceOf(SlicerHighSpeedAntagonist.class);
        assertThat(gqs.isCreature(gd, slicer)).isTrue();
    }

    @Test
    void decliningOpponentUpkeepChoiceConvertsSlicer() {
        Permanent slicer = harness.addToBattlefieldAndReturn(player1, new SlicerHiredMuscle());

        advanceToOpponentUpkeep();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(slicer.isTransformed()).isTrue();
        assertThat(slicer.getCard()).isInstanceOf(SlicerHighSpeedAntagonist.class);
    }

    @Test
    void acceptingOpponentUpkeepChoiceStealsUntapsAndProtectsSlicerUntilEndOfTurn() {
        Permanent slicer = harness.addToBattlefieldAndReturn(player1, new SlicerHiredMuscle());
        slicer.tap();

        advanceToOpponentUpkeep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.findPermanentController(gd, slicer.getId())).isEqualTo(player2.getId());
        assertThat(slicer.isTapped()).isFalse();
        assertThat(slicer.isTransformed()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        assertThat(gqs.cantBeSacrificed(gd, slicer)).isTrue();

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gqs.findPermanentController(gd, slicer.getId())).isEqualTo(player1.getId());
        assertThat(gqs.cantBeSacrificed(gd, slicer)).isFalse();
    }

    @Test
    void combatDamageWithConvertedSlicerConvertsItBack() {
        Permanent slicer = castConvertedSlicer();
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);
        harness.assertLife(player2, 17);
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveAllTriggers);

        assertThat(slicer.isTransformed()).isTrue();
        assertThat(slicer.getCard()).isInstanceOf(SlicerHighSpeedAntagonist.class);

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.assertLife(player2, 17);

        assertThat(slicer.isTransformed()).isFalse();
        assertThat(slicer.getCard()).isInstanceOf(SlicerHiredMuscle.class);
    }

    @Test
    void acceptingOpponentUpkeepChoiceGoadsSlicerUntilOwnersNextTurn() {
        Permanent slicer = harness.addToBattlefieldAndReturn(player1, new SlicerHiredMuscle());

        advanceToOpponentUpkeep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.isGoaded(gd, slicer)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, slicer)).isGreaterThan(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gqs.isGoaded(gd, slicer)).isTrue();

        advanceToUpkeep(player1);
        assertThat(gqs.isGoaded(gd, slicer)).isFalse();
    }

    @Test
    void livingMetalDoesNotMakeConvertedSlicerACreatureDuringOpponentsTurn() {
        Permanent slicer = castConvertedSlicer();

        advanceToOpponentUpkeep();

        assertThat(gqs.isCreature(gd, slicer)).isFalse();
        assertThat(slicer.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();

        advanceToUpkeep(player1);
        assertThat(gqs.isCreature(gd, slicer)).isTrue();
    }

    @Test
    void regularCastingKeepsFrontFaceAndDoesNotTriggerDuringControllersUpkeep() {
        harness.castFromHand(player1, new SlicerHiredMuscle(), "{4}{R}");
        resolveAllTriggers();
        Permanent slicer = findPermanent(player1, "Slicer, Hired Muscle");

        advanceToUpkeep(player1);

        assertThat(slicer.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent castConvertedSlicer() {
        harness.setHand(player1, List.of(new SlicerHiredMuscle()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Slicer, High-Speed Antagonist");
    }

    private void advanceToOpponentUpkeep() {
        advanceToUpkeep(player2);
    }
}
