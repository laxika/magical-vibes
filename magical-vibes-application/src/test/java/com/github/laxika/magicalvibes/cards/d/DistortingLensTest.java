package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DistortingLensTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: target permanent becomes the chosen color, replacing its previous colors")
    void targetBecomesChosenColor() {
        harness.addToBattlefield(player1, new DistortingLens());
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, bearsId);
        harness.passBothPriorities();

        // Resolving the ability prompts the controller for a color.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
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
    @DisplayName("Chosen color wears off at end of turn")
    void colorWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new DistortingLens());
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, bearsId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.RED);

        // The floating layer-5 color setter expires at cleanup.
        gd.expireEndOfTurnFloatingEffects();
        bears.resetModifiers();

        assertThat(gqs.getEffectiveColors(gd, bears)).doesNotContain(CardColor.RED);
    }
}
