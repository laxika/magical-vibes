package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TidalVisionaryTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature becomes the chosen color until end of turn")
    void targetCreatureBecomesChosenColor() {
        Permanent visionary = addReadyVisionary();
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        Permanent target = findPermanent(player2, "Grizzly Bears");
        assertThat(visionary.isTapped()).isTrue();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("The chosen color wears off at end of turn")
    void chosenColorWearsOffAtEndOfTurn() {
        addReadyVisionary();
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        Permanent target = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.RED);

        gd.expireEndOfTurnFloatingEffects();
        target.resetModifiers();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
    }

    private Permanent addReadyVisionary() {
        Permanent visionary = new Permanent(new TidalVisionary());
        visionary.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(visionary);
        return visionary;
    }
}
