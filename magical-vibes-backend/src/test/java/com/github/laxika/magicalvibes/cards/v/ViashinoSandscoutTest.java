package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ViashinoSandscoutTest extends BaseCardTest {


    @Test
    @DisplayName("Viashino Sandscout has correct card properties and end-step return trigger")
    void hasCorrectProperties() {
        ViashinoSandscout card = new ViashinoSandscout();

        assertThat(card.getEffects(EffectSlot.END_STEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.END_STEP_TRIGGERED).getFirst())
                .isInstanceOf(ReturnSelfToHandEffect.class);
    }

    @Test
    @DisplayName("Triggers at end step and returns itself to owner's hand on resolution")
    void triggersAtEndStepAndReturnsItselfToHand() {
        Permanent sandscout = new Permanent(new ViashinoSandscout());
        gd.playerBattlefields.get(player1.getId()).add(sandscout);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Viashino Sandscout");
        assertThat(trigger.getSourcePermanentId()).isEqualTo(sandscout.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Viashino Sandscout"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Viashino Sandscout"));
    }
}

