package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloisteredYouth.class})
class CloisteredYouthTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms when you choose yes at upkeep")
    void transformsWhenChosenAtUpkeep() {
        Permanent youth = harness.addToBattlefieldAndReturn(player1, new CloisteredYouth());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability → MayEffect prompts
        harness.handleMayAbilityChosen(player1, true);

        assertThat(youth.isTransformed()).isTrue();
        assertThat(youth.getCard().getName()).isEqualTo("Unholy Fiend");
        assertThat(gqs.getEffectivePower(gd, youth)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, youth)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not transform when you choose no at upkeep")
    void doesNotTransformWhenDeclined() {
        Permanent youth = harness.addToBattlefieldAndReturn(player1, new CloisteredYouth());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability → MayEffect prompts
        harness.handleMayAbilityChosen(player1, false);

        assertThat(youth.isTransformed()).isFalse();
        assertThat(youth.getCard().getName()).isEqualTo("Cloistered Youth");
    }

    @Test
    @DisplayName("Does not trigger on opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        Permanent youth = harness.addToBattlefieldAndReturn(player1, new CloisteredYouth());

        advanceToUpkeep(player2);

        assertThat(youth.isTransformed()).isFalse();
        assertThat(youth.getCard().getName()).isEqualTo("Cloistered Youth");
    }

    @Test
    @DisplayName("Unholy Fiend causes controller to lose 1 life at end step")
    void unholyFiendLosesLifeAtEndStep() {
        Permanent youth = harness.addToBattlefieldAndReturn(player1, new CloisteredYouth());

        // Transform to Unholy Fiend
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(youth.isTransformed()).isTrue();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        // Advance to end step
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        // End step trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Unholy Fiend does not cause life loss on opponent's end step")
    void unholyFiendNoLifeLossOnOpponentEndStep() {
        Permanent youth = harness.addToBattlefieldAndReturn(player1, new CloisteredYouth());

        // Transform to Unholy Fiend
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(youth.isTransformed()).isTrue();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        // Opponent's end step
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        // No trigger should fire
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cloistered Youth causes no life loss while it remains on its front face")
    void frontFaceDoesNotLoseLifeAtEndStep() {
        Permanent youth = harness.addToBattlefieldAndReturn(player1, new CloisteredYouth());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).isEmpty();
        assertThat(youth.isTransformed()).isFalse();
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Unholy Fiend stays transformed without an upkeep trigger")
    void backFaceDoesNotTransformBackAtUpkeep() {
        Permanent youth = harness.addToBattlefieldAndReturn(player1, new CloisteredYouth());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(youth.isTransformed()).isTrue();
        assertThat(youth.getCard().getName()).isEqualTo("Unholy Fiend");
    }

}
