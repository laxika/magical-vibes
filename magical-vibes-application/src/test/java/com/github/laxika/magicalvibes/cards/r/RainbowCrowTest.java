package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RainbowCrow.class})
class RainbowCrowTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability prompts for a color without requiring a target")
    void activatingPromptsForColor() {
        addCreatureReady(player1, new RainbowCrow());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("The chosen color replaces Rainbow Crow's color until end of turn")
    void becomesChosenColorUntilEndOfTurn() {
        Permanent crow = addCreatureReady(player1, new RainbowCrow());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.getEffectiveColors(gd, crow)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("The chosen color wears off at end of turn")
    void chosenColorWearsOffAtEndOfTurn() {
        Permanent crow = addCreatureReady(player1, new RainbowCrow());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        crow.resetModifiers();
        gd.expireEndOfTurnFloatingEffects();

        assertThat(gqs.getEffectiveColors(gd, crow)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Activating the ability requires paying {1}")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new RainbowCrow());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
