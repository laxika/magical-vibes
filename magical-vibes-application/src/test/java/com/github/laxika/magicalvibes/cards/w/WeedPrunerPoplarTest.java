package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

class WeedPrunerPoplarTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger presents mandatory target selection")
    void upkeepTriggerPresentsTargetSelection() {
        addCreatureReady(player1, new WeedPrunerPoplar());
        addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Chosen creature gets -1/-1 until end of turn")
    void chosenCreatureGetsMinusOneMinusOne() {
        addCreatureReady(player1, new WeedPrunerPoplar());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());

        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(-1);
        assertThat(bears.getToughnessModifier()).isEqualTo(-1);
        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target the controller's own creatures")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new WeedPrunerPoplar());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(-1);
        assertThat(bears.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Does not trigger when the Poplar is the only creature (cannot target itself)")
    void doesNotTriggerWhenOnlyCreature() {
        addCreatureReady(player1, new WeedPrunerPoplar());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("The -1/-1 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        addCreatureReady(player1, new WeedPrunerPoplar());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }
}
