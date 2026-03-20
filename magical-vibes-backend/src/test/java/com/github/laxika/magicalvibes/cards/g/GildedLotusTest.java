package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GildedLotusTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Gilded Lotus prompts for mana color")
    void activateAbilityPromptsManaColor() {
        harness.addToBattlefield(player1, new GildedLotus());
        GameData gd = harness.getGameData();
        Permanent lotus = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, null, null);

        assertThat(lotus.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
        assertThat(gd.interaction.colorChoice().playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a color adds exactly three mana of that color")
    void choosingColorAddsThreeMana() {
        for (String color : List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN")) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            harness.addToBattlefield(player1, new GildedLotus());
            GameData gd = harness.getGameData();
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, null, null);
            int before = gd.playerManaPools.get(player1.getId()).get(manaColor);

            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(before + 3);
            assertThat(gd.interaction.awaitingInputType()).isNull();
        }
    }

    @Test
    @DisplayName("Cannot activate Gilded Lotus when already tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new GildedLotus());
        GameData gd = harness.getGameData();
        Permanent lotus = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
