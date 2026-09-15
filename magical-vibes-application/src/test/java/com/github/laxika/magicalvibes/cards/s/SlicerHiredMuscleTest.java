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
        slicer.setSummoningSick(false);
        slicer.setAttacking(true);
        slicer.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();
        gd.interaction.clearAwaitingInput();

        harness.resolveCombatDamage();
        harness.assertLife(player2, 17);
        resolveAllTriggers();

        assertThat(slicer.isTransformed()).isFalse();
        assertThat(slicer.getCard()).isInstanceOf(SlicerHiredMuscle.class);
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
