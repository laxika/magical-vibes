package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WoodedBastion.class})
class WoodedBastionTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for {C} adds one colorless mana without using the stack")
    void tapForColorless() {
        harness.addToBattlefield(player1, new WoodedBastion());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Filter ability spends one {G/W} and produces two mana in the chosen combination")
    void filterProducesTwoChosenMana() {
        String[][] combos = {{"GREEN", "GREEN"}, {"GREEN", "WHITE"}, {"WHITE", "WHITE"}};
        for (String[] combo : combos) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            harness.addToBattlefield(player1, new WoodedBastion());
            harness.addMana(player1, ManaColor.GREEN, 1); // pays the {G/W} activation cost
            GameData gd = harness.getGameData();

            harness.activateAbility(player1, 0, 1, null, null);
            harness.handleListChoice(player1, combo[0]);
            harness.handleListChoice(player1, combo[1]);

            int expectedGreen = (combo[0].equals("GREEN") ? 1 : 0) + (combo[1].equals("GREEN") ? 1 : 0);
            int expectedWhite = 2 - expectedGreen;
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(expectedGreen);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(expectedWhite);
            assertThat(gd.stack).isEmpty();
        }
    }

    @Test
    @DisplayName("Filter ability cannot be activated without a {G/W} mana to pay")
    void filterRequiresManaCost() {
        harness.addToBattlefield(player1, new WoodedBastion());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("White mana pays the hybrid cost and can be converted to two green mana")
    void filterCanBePaidWithWhite() {
        var bastion = harness.addToBattlefieldAndReturn(player1, new WoodedBastion());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(bastion.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"COLORLESS", "BLUE", "BLACK", "RED"})
    @DisplayName("Mana other than green or white cannot pay the hybrid activation cost")
    void filterRejectsOtherMana(ManaColor color) {
        var bastion = harness.addToBattlefieldAndReturn(player1, new WoodedBastion());
        harness.addMana(player1, color, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bastion.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Neither mana ability can be activated after the land has tapped for mana")
    void tappedLandCannotActivateAgain(int abilityIndex) {
        var bastion = harness.addToBattlefieldAndReturn(player1, new WoodedBastion());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bastion.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"COLORLESS", "BLUE", "BLACK", "RED"})
    @DisplayName("The filter ability only produces green or white mana")
    void filterRejectsOtherOutputColors(ManaColor color) {
        harness.addToBattlefield(player1, new WoodedBastion());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.handleListChoice(player1, color.name()))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, "WHITE");
        assertThatThrownBy(() -> harness.handleListChoice(player1, color.name()))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
