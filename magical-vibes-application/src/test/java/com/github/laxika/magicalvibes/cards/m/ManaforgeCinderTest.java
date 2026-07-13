package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManaforgeCinderTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability prompts a choice between black and red")
    void activatingPromptsBlackOrRedChoice() {
        harness.addToBattlefield(player1, new ManaforgeCinder());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("BLACK", "RED");
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsThatMana() {
        for (String color : new String[]{"BLACK", "RED"}) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            harness.addToBattlefield(player1, new ManaforgeCinder());
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            GameData gd = harness.getGameData();
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, color);

            // {1} paid in, one mana of the chosen color produced.
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    @DisplayName("The ability can be activated no more than three times each turn")
    void limitedToThreeActivationsPerTurn() {
        Player p = player1;
        harness.addToBattlefield(p, new ManaforgeCinder());
        harness.addMana(p, ManaColor.COLORLESS, 4);
        GameData gd = harness.getGameData();

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(p, 0, 0, null, null);
            harness.handleListChoice(p, "RED");
        }

        assertThat(gd.playerManaPools.get(p.getId()).get(ManaColor.RED)).isEqualTo(3);

        assertThatThrownBy(() -> harness.activateAbility(p, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 3");
    }
}
