package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShimmeringEfreetTest extends BaseCardTest {

    @Test
    @DisplayName("Phasing in presents a mandatory creature target to phase out")
    void phasesInPresentsTargetChoice() {
        Permanent efreet = addCreatureReady(player1, new ShimmeringEfreet());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        advanceTurn(); // player2
        advanceTurn(); // player1 untap — efreet phases out

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(efreet);

        advanceTurn(); // player2
        advanceTurn(); // player1 untap — efreet phases in → upkeep target choice

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(efreet);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(bears);
        // The chosen target phases out, never the Efreet that granted the ability.
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(efreet);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).doesNotContain(efreet);
    }

    @Test
    @DisplayName("Can phase out itself when it phases in")
    void canTargetSelf() {
        Permanent efreet = addCreatureReady(player1, new ShimmeringEfreet());

        advanceTurn();
        advanceTurn(); // phases out
        advanceTurn();
        advanceTurn(); // phases in

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, efreet.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(efreet);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(efreet);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
