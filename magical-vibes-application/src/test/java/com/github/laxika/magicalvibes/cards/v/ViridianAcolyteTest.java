package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ViridianAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate Viridian Acolyte while it has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new ViridianAcolyte());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsMana() {
        for (String color : List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN")) {
            harness = new com.github.laxika.magicalvibes.testutil.GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            harness.addToBattlefield(player1, new ViridianAcolyte());
            GameData gameData = harness.getGameData();
            Permanent acolyte = gameData.playerBattlefields.get(player1.getId()).getFirst();
            acolyte.setSummoningSick(false);
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, null, null);

            assertThat(acolyte.isTapped()).isTrue();
            assertThat(gameData.stack).isEmpty();
            assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
            int before = gameData.playerManaPools.get(player1.getId()).get(manaColor);
            harness.handleListChoice(player1, color);

            assertThat(gameData.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(before + 1);
            assertThat(gameData.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    @DisplayName("Cannot activate Viridian Acolyte when it is already tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new ViridianAcolyte());
        GameData gameData = harness.getGameData();
        Permanent acolyte = gameData.playerBattlefields.get(player1.getId()).getFirst();
        acolyte.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
