package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObeliskOfNaya.class})
class ObeliskOfNayaTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the tap ability prompts a choice between red, green and white")
    void activatingPromptsColorChoice() {
        harness.addToBattlefield(player1, new ObeliskOfNaya());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("RED", "GREEN", "WHITE");
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsThatMana() {
        for (String color : new String[]{"RED", "GREEN", "WHITE"}) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            harness.addToBattlefield(player1, new ObeliskOfNaya());
            GameData gd = harness.getGameData();
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    void unavailableColorsAreRejectedWithoutCompletingTheChoice() {
        harness.addToBattlefield(player1, new ObeliskOfNaya());
        harness.activateAbility(player1, 0, 0, null, null);

        for (String color : new String[]{"BLUE", "BLACK", "COLORLESS"}) {
            assertThatThrownBy(() -> harness.handleListChoice(player1, color))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        }

        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void activationPaysTapCostAndCannotBeRepeatedWhileTapped() {
        Permanent obelisk = harness.addToBattlefieldAndReturn(player1, new ObeliskOfNaya());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(obelisk.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
