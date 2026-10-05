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

@CardUsed({ObeliskOfBant.class})
class ObeliskOfBantTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the tap ability prompts a choice between green, white and blue")
    void activatingPromptsColorChoice() {
        harness.addToBattlefield(player1, new ObeliskOfBant());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("GREEN", "WHITE", "BLUE");
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsThatMana() {
        for (String color : new String[]{"GREEN", "WHITE", "BLUE"}) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            harness.addToBattlefield(player1, new ObeliskOfBant());
            GameData gd = harness.getGameData();
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    @DisplayName("A newly controlled noncreature Obelisk can tap for mana immediately")
    void newlyControlledObeliskCanActivate() {
        Permanent obelisk = harness.addToBattlefieldAndReturn(player1, new ObeliskOfBant());
        obelisk.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(obelisk.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Obelisk cannot activate again while tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new ObeliskOfBant());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Colors outside green, white and blue are rejected without consuming the choice")
    void rejectsUnavailableManaColors() {
        harness.addToBattlefield(player1, new ObeliskOfBant());
        harness.activateAbility(player1, 0, 0, null, null);

        for (String color : new String[]{"RED", "BLACK", "COLORLESS"}) {
            assertThatThrownBy(() -> harness.handleListChoice(player1, color))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        }

        harness.handleListChoice(player1, "BLUE");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
