package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObeliskOfJund.class})
class ObeliskOfJundTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the tap ability prompts a choice between black, red and green")
    void activatingPromptsColorChoice() {
        harness.addToBattlefield(player1, new ObeliskOfJund());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("BLACK", "RED", "GREEN");
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsThatMana() {
        for (String color : new String[]{"BLACK", "RED", "GREEN"}) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            harness.addToBattlefield(player1, new ObeliskOfJund());
            GameData gd = harness.getGameData();
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    @DisplayName("Activation pays the tap cost and cannot be repeated while tapped")
    void activationPaysTapCost() {
        var obelisk = harness.addToBattlefieldAndReturn(player1, new ObeliskOfJund());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(obelisk.isTapped()).isTrue();
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A newly entered Obelisk adds mana for its controller during the opponent's turn")
    void addsManaForControllerDuringOpponentsTurn() {
        var obelisk = harness.addToBattlefieldAndReturn(player2, new ObeliskOfJund());

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, "BLACK");

        assertThat(obelisk.isTapped()).isTrue();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player2.getId()).get(color))
                    .isEqualTo(color == ManaColor.BLACK ? 1 : 0);
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
