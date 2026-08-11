package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RainbowCrowTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability prompts for a color without requiring a target")
    void activatingPromptsForColor() {
        addReadyCrow();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("The chosen color replaces Rainbow Crow's color until end of turn")
    void becomesChosenColorUntilEndOfTurn() {
        Permanent crow = addReadyCrow();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.getEffectiveColors(gd, crow)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("The chosen color wears off at end of turn")
    void chosenColorWearsOffAtEndOfTurn() {
        Permanent crow = addReadyCrow();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        crow.resetModifiers();
        gd.expireEndOfTurnFloatingEffects();

        assertThat(gqs.getEffectiveColors(gd, crow)).containsExactly(CardColor.BLUE);
    }

    private Permanent addReadyCrow() {
        Permanent crow = new Permanent(new RainbowCrow());
        crow.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(crow);
        return crow;
    }
}
