package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpalineUnicornTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate Opaline Unicorn while it has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new OpalineUnicorn());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Activating Opaline Unicorn taps it and prompts for mana color")
    void activateAbilityPromptsManaColor() {
        harness.addToBattlefield(player1, new OpalineUnicorn());
        GameData gd = harness.getGameData();
        Permanent unicorn = gd.playerBattlefields.get(player1.getId()).getFirst();
        unicorn.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(unicorn.isTapped()).isTrue();
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

            harness.addToBattlefield(player1, new OpalineUnicorn());
            GameData gd = harness.getGameData();
            Permanent unicorn = gd.playerBattlefields.get(player1.getId()).getFirst();
            unicorn.setSummoningSick(false);
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, null, null);
            int before = gd.playerManaPools.get(player1.getId()).get(manaColor);

            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(before + 1);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    @DisplayName("Cannot activate Opaline Unicorn when it is already tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new OpalineUnicorn());
        GameData gd = harness.getGameData();
        Permanent unicorn = gd.playerBattlefields.get(player1.getId()).getFirst();
        unicorn.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
