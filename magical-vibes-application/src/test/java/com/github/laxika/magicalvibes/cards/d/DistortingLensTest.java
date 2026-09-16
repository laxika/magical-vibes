package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DistortingLens.class, GrizzlyBears.class})
class DistortingLensTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: target permanent becomes the chosen color, replacing its previous colors")
    void targetBecomesChosenColor() {
        Permanent lens = harness.addToBattlefieldAndReturn(player1, new DistortingLens());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        assertThat(lens.isTapped()).isTrue();
        harness.passBothPriorities();

        // Resolving the ability prompts the controller for a color.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        // Green Grizzly Bears becomes red only (CR 105.3 — replaces all previous colors).
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("Can target a permanent controlled by an opponent")
    void canTargetOpponentsPermanent() {
        harness.addToBattlefield(player1, new DistortingLens());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.WHITE);
    }

    @Test
    @DisplayName("Any permanent is a legal target, including a colorless one")
    void canTargetColorlessPermanent() {
        Permanent lens = harness.addToBattlefieldAndReturn(player1, new DistortingLens());

        harness.activateAbility(player1, 0, 0, null, lens.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        // The colorless artifact itself becomes blue.
        assertThat(gqs.getEffectiveColors(gd, lens)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Chosen color wears off at end of turn")
    void colorWearsOffAtEndOfTurn() {
        Permanent lens = harness.addToBattlefieldAndReturn(player1, new DistortingLens());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        assertThat(lens.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.RED);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.GREEN);
    }
}
