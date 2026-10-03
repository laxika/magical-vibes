package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed(BlackLotus.class)
class BlackLotusTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping and sacrificing Black Lotus adds three mana of the chosen color")
    void sacrificeAddsThreeManaOfChosenColor() {
        harness.addToBattlefield(player1, new BlackLotus());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Black Lotus");
        harness.assertInGraveyard(player1, "Black Lotus");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, ManaColor.BLACK.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Black Lotus produces three mana of exactly one chosen color without using the stack")
    void producesThreeManaOfAnyOneColorImmediately(ManaColor chosenColor) {
        harness.addToBattlefield(player1, new BlackLotus());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Black Lotus");
        harness.assertInGraveyard(player1, "Black Lotus");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        var choice = (PendingInteraction.ColorChoice) gd.interaction.activeInteraction();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");

        harness.handleListChoice(player1, chosenColor.name());

        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color))
                    .isEqualTo(color == chosenColor ? 3 : 0);
            assertThat(gd.playerManaPools.get(player2.getId()).get(color)).isZero();
        }
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A tapped Black Lotus cannot activate or pay its sacrifice cost")
    void tappedLotusCannotActivate() {
        var lotus = harness.addToBattlefieldAndReturn(player1, new BlackLotus());
        lotus.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.assertOnBattlefield(player1, "Black Lotus");
        harness.assertNotInGraveyard(player1, "Black Lotus");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
