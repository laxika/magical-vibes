package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ViashinoCutthroatTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers at end step and returns itself to owner's hand")
    void triggersAtEndStepAndReturnsToHand() {
        Permanent cutthroat = new Permanent(new ViashinoCutthroat());
        gd.playerBattlefields.get(player1.getId()).add(cutthroat);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getSourcePermanentId()).isEqualTo(cutthroat.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Viashino Cutthroat");
        harness.assertInHand(player1, "Viashino Cutthroat");
    }

    @Test
    @DisplayName("Triggers on the opponent's end step")
    void triggersOnOpponentsEndStep() {
        Permanent cutthroat = new Permanent(new ViashinoCutthroat());
        gd.playerBattlefields.get(player1.getId()).add(cutthroat);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Viashino Cutthroat");
        harness.assertInHand(player1, "Viashino Cutthroat");
    }
}
