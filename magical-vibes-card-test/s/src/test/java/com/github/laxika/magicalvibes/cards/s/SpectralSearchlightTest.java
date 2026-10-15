package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SpectralSearchlight.class)
class SpectralSearchlightTest extends BaseCardTest {

    @Test
    @DisplayName("Chosen player receives the chosen color of mana")
    void chosenPlayerReceivesChosenColor() {
        harness.addToBattlefield(player1, new SpectralSearchlight());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Controller may receive the chosen color and the source is tapped")
    void controllerMayReceiveChosenColor() {
        harness.addToBattlefield(player1, new SpectralSearchlight());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The recipient may choose any of the five colors")
    void recipientMayChooseAnyColor(ManaColor color) {
        harness.addToBattlefield(player1, new SpectralSearchlight());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handleListChoice(player2, color.name());

        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player2.getId()).get(manaColor))
                    .isEqualTo(manaColor == color ? 1 : 0);
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isZero();
        }
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Colorless is not a color choice and a valid choice remains possible")
    void cannotChooseColorless() {
        harness.addToBattlefield(player1, new SpectralSearchlight());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThatThrownBy(() -> harness.handleListChoice(player2, ManaColor.COLORLESS.name()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.handleListChoice(player2, ManaColor.RED.name());
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped Searchlight cannot activate again")
    void cannotActivateAgainWhileTapped() {
        harness.addToBattlefield(player1, new SpectralSearchlight());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
