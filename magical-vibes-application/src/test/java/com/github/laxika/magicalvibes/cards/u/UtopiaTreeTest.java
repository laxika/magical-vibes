package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(UtopiaTree.class)
class UtopiaTreeTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate Utopia Tree while it has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new UtopiaTree());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Activating Utopia Tree taps it and prompts for mana color")
    void activateAbilityPromptsManaColor() {
        Permanent tree = addCreatureReady(player1, new UtopiaTree());

        harness.activateAbility(player1, 0, null, null);

        assertThat(tree.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED", "GREEN");
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsMana() {
        for (String color : List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN")) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            gd = harness.getGameData();
            addCreatureReady(player1, new UtopiaTree());
            for (ManaColor existingColor : ManaColor.values()) {
                harness.addMana(player1, existingColor, 1);
            }
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, null, null);

            harness.handleListChoice(player1, color);

            for (ManaColor existingColor : ManaColor.values()) {
                int expected = existingColor == manaColor ? 2 : 1;
                assertThat(gd.playerManaPools.get(player1.getId()).get(existingColor))
                        .as("mana of %s", existingColor)
                        .isEqualTo(expected);
            }
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    @DisplayName("Cannot activate Utopia Tree when it is already tapped")
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new UtopiaTree());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
