package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DistortingLens.class, GrizzlyBears.class, Shatter.class})
class DistortingLensTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: target permanent becomes the chosen color, replacing its previous colors")
    void targetBecomesChosenColor() {
        harness.addToBattlefield(player1, new DistortingLens());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        UUID bearsId = bears.getId();
        harness.activateAbility(player1, 0, 0, null, bearsId);
        harness.passBothPriorities();

        // Resolving the ability prompts the controller for a color.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        // Green Grizzly Bears becomes red only (CR 105.3 — replaces all previous colors).
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("Any permanent is a legal target, including a colorless one")
    void canTargetColorlessPermanent() {
        harness.addToBattlefield(player1, new DistortingLens());

        UUID lensId = harness.getPermanentId(player1, "Distorting Lens");
        harness.activateAbility(player1, 0, 0, null, lensId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        Permanent lens = gd.playerBattlefields.get(player1.getId()).get(0);
        // The colorless artifact itself becomes blue.
        assertThat(gqs.getEffectiveColors(gd, lens)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Can target an opponent's permanent")
    void canTargetOpponentsPermanent() {
        harness.addToBattlefield(player1, new DistortingLens());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        UUID bearsId = bears.getId();
        harness.activateAbility(player1, 0, 0, null, bearsId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.WHITE);
    }

    @Test
    @DisplayName("Chosen color wears off at end of turn")
    void colorWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new DistortingLens());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        UUID bearsId = bears.getId();
        harness.activateAbility(player1, 0, 0, null, bearsId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.RED);

        // The floating layer-5 color setter expires at cleanup.
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.GREEN);
    }
    @Test
    @DisplayName("The most recently resolved color change replaces earlier color changes")
    void laterColorChangeWins() {
        harness.addToBattlefield(player1, new DistortingLens());
        harness.addToBattlefield(player1, new DistortingLens());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.BLACK);

        harness.activateAbility(player1, 1, null, bears.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.BLUE);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("The ability resolves even if Distorting Lens is destroyed in response")
    void abilitySurvivesSourceRemoval() {
        Permanent lens = harness.addToBattlefieldAndReturn(player1, new DistortingLens());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.castAndResolveInstant(player2, 0, lens.getId());
        harness.assertNotOnBattlefield(player1, "Distorting Lens");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("An ability with a removed target does not prompt for a color")
    void removedTargetDoesNotPromptForColor() {
        Permanent lens = harness.addToBattlefieldAndReturn(player1, new DistortingLens());
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, lens.getId());
        harness.castAndResolveInstant(player2, 0, lens.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Distorting Lens");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }
}
