package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({Treasure.class})
class TreasureTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Treasure adds one mana of the chosen color")
    void sacrificesForAnyColorMana() {
        Treasure treasure = new Treasure();
        treasure.setToken(true);
        harness.addToBattlefield(player1, treasure);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Treasure");
        harness.assertNotInGraveyard(player1, "Treasure");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "GREEN"})
    void producesExactlyOneManaOfEachOtherColorWithoutUsingTheStack(ManaColor color) {
        Treasure treasure = new Treasure();
        treasure.setToken(true);
        harness.addToBattlefield(player1, treasure);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();

        harness.handleListChoice(player1, color.name());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
    }

    @Test
    void tappedTreasureCannotBeSacrificedForMana() {
        Treasure treasure = new Treasure();
        treasure.setToken(true);
        harness.addToBattlefieldAndReturn(player1, treasure).tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        harness.assertOnBattlefield(player1, "Treasure");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manaGoesToTheActivatingController() {
        Treasure treasure = new Treasure();
        treasure.setToken(true);
        harness.addToBattlefield(player2, treasure);

        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "BLUE");

        harness.assertNotOnBattlefield(player2, "Treasure");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
