package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CloisteredYouthTest extends BaseCardTest {

    // ===== Card configuration =====

    @Test
    @DisplayName("Has correct effects configured")
    void hasCorrectEffects() {
        CloisteredYouth card = new CloisteredYouth();

        // Front face: upkeep may-transform trigger
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(TransformSelfEffect.class);

        // Back face exists with end step life loss
        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceCard().getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED)).hasSize(1);
        assertThat(card.getBackFaceCard().getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst())
                .isInstanceOf(LoseLifeEffect.class);
        LoseLifeEffect loseLife = (LoseLifeEffect) card.getBackFaceCard().getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst();
        assertThat(loseLife.amount()).isEqualTo(1);
    }

    // ===== Front face: upkeep transform trigger =====

    @Test
    @DisplayName("Transforms when you choose yes at upkeep")
    void transformsWhenChosenAtUpkeep() {
        harness.addToBattlefield(player1, new CloisteredYouth());
        Permanent youth = findPermanent(player1, "Cloistered Youth");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
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
        harness.addToBattlefield(player1, new CloisteredYouth());
        Permanent youth = findPermanent(player1, "Cloistered Youth");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability → MayEffect prompts
        harness.handleMayAbilityChosen(player1, false);

        assertThat(youth.isTransformed()).isFalse();
        assertThat(youth.getCard().getName()).isEqualTo("Cloistered Youth");
        assertThat(gqs.getEffectivePower(gd, youth)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, youth)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger on opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new CloisteredYouth());
        Permanent youth = findPermanent(player1, "Cloistered Youth");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger for player1

        assertThat(youth.isTransformed()).isFalse();
        assertThat(youth.getCard().getName()).isEqualTo("Cloistered Youth");
    }

    // ===== Back face: Unholy Fiend end step life loss =====

    @Test
    @DisplayName("Unholy Fiend causes controller to lose 1 life at end step")
    void unholyFiendLosesLifeAtEndStep() {
        harness.addToBattlefield(player1, new CloisteredYouth());
        Permanent youth = findPermanent(player1, "Cloistered Youth");

        // Transform to Unholy Fiend
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
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
        harness.addToBattlefield(player1, new CloisteredYouth());
        Permanent youth = findPermanent(player1, "Cloistered Youth");

        // Transform to Unholy Fiend
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
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

    // ===== Helpers =====

    private Permanent findPermanent(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
