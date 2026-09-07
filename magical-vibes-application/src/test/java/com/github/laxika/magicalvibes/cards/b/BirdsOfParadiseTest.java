package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BirdsOfParadise.class)
class BirdsOfParadiseTest extends BaseCardTest {

    

    @Test
    @DisplayName("Cannot activate Birds of Paradise while it has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new BirdsOfParadise());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Activating Birds of Paradise prompts for mana color immediately")
    void activateAbilityPromptsManaColorImmediately() {
        Permanent birds = addCreatureReady(player1, new BirdsOfParadise());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);

        assertThat(birds.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsMana() {
        for (String color : List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN")) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            Permanent birds = harness.addToBattlefieldAndReturn(player1, new BirdsOfParadise());
            birds.setSummoningSick(false);
            GameData gd = harness.getGameData();
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, null, null);
            int before = gd.playerManaPools.get(player1.getId()).get(manaColor);

            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(before + 1);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    void cannotChooseColorlessMana() {
        Permanent birds = addCreatureReady(player1, new BirdsOfParadise());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handleListChoice(player1, ManaColor.COLORLESS.name()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Cannot activate Birds of Paradise when it is already tapped")
    void cannotActivateWhileTapped() {
        Permanent birds = addCreatureReady(player1, new BirdsOfParadise());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
