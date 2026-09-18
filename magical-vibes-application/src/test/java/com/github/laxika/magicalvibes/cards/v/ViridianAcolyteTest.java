package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed(ViridianAcolyte.class)
class ViridianAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate Viridian Acolyte while it has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new ViridianAcolyte());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsMana() {
        for (String color : List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN")) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            GameData gameData = harness.getGameData();
            gd = gameData;
            Permanent acolyte = harness.addToBattlefieldAndReturn(player1, new ViridianAcolyte());
            acolyte.setSummoningSick(false);
            ManaColor manaColor = ManaColor.valueOf(color);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.activateAbility(player1, 0, null, null);

            assertThat(acolyte.isTapped()).isTrue();
            assertThat(gameData.stack).isEmpty();
            assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
            assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
            int before = gameData.playerManaPools.get(player1.getId()).get(manaColor);
            harness.handleListChoice(player1, color);

            assertThat(gameData.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(before + 1);
            assertThat(gameData.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    @DisplayName("Cannot activate Viridian Acolyte without paying its generic activation cost")
    void cannotActivateWithoutMana() {
        Permanent acolyte = harness.addToBattlefieldAndReturn(player1, new ViridianAcolyte());
        acolyte.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate Viridian Acolyte when it is already tapped")
    void cannotActivateWhileTapped() {
        Permanent acolyte = harness.addToBattlefieldAndReturn(player1, new ViridianAcolyte());
        acolyte.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
