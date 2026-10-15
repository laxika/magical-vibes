package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CascadeBluffs.class})
class CascadeBluffsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for {C} adds one colorless mana without using the stack")
    void tapForColorless() {
        harness.addToBattlefield(player1, new CascadeBluffs());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Filter ability spends one {U/R} and produces two mana in the chosen combination")
    void filterProducesTwoChosenMana() {
        String[][] combos = {{"BLUE", "BLUE"}, {"BLUE", "RED"}, {"RED", "RED"}};
        for (String[] combo : combos) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            harness.addToBattlefield(player1, new CascadeBluffs());
            harness.addMana(player1, ManaColor.BLUE, 1); // pays the {U/R} activation cost
            GameData gd = harness.getGameData();

            harness.activateAbility(player1, 0, 1, null, null);
            harness.handleListChoice(player1, combo[0]);
            harness.handleListChoice(player1, combo[1]);

            int expectedBlue = (combo[0].equals("BLUE") ? 1 : 0) + (combo[1].equals("BLUE") ? 1 : 0);
            int expectedRed = 2 - expectedBlue;
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(expectedBlue);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(expectedRed);
            assertThat(gd.stack).isEmpty();
        }
    }

    @Test
    @DisplayName("Filter ability cannot be activated without a {U/R} mana to pay")
    void filterRequiresManaCost() {
        harness.addToBattlefield(player1, new CascadeBluffs());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @CsvSource({"BLUE, BLUE, 2, 0", "BLUE, RED, 1, 1", "RED, RED, 0, 2"})
    void filterAcceptsRedPayment(String first, String second, int blue, int red) {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CascadeBluffs());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, first);
        harness.handleListChoice(player1, second);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(blue);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(red);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"COLORLESS", "WHITE", "BLACK", "GREEN"})
    void filterRejectsOtherManaTypes(ManaColor color) {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CascadeBluffs());
        harness.addMana(player1, color, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
    }

    @Test
    void colorlessAbilityPaysTapCost() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CascadeBluffs());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }
}
