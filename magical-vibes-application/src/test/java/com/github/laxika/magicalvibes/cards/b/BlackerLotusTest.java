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

@CardUsed({BlackerLotus.class})
class BlackerLotusTest extends BaseCardTest {

    @Test
    @DisplayName("Tears itself up and adds four mana of the chosen color")
    void removesItselfAndAddsFourMana() {
        harness.addToBattlefield(player1, new BlackerLotus());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.assertNotOnBattlefield(player1, "Blacker Lotus");

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Blacker Lotus");
        harness.assertNotInHand(player1, "Blacker Lotus");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Adds all four mana in exactly one chosen color")
    void addsFourManaOfAnyOneColor(ManaColor chosenColor) {
        harness.addToBattlefield(player1, new BlackerLotus());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, chosenColor.name());

        assertThat(gd.stack).isEmpty();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color))
                    .isEqualTo(color == chosenColor ? 4 : 0);
            assertThat(gd.playerManaPools.get(player2.getId()).get(color)).isZero();
        }
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        var lotus = harness.addToBattlefieldAndReturn(player1, new BlackerLotus());
        lotus.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Blacker Lotus");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
    }
}
