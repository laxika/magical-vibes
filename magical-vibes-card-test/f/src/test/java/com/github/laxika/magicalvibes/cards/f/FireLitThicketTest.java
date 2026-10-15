package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed({FireLitThicket.class})
class FireLitThicketTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for {C} adds one colorless mana without using the stack")
    void tapForColorless() {
        harness.addToBattlefield(player1, new FireLitThicket());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Filter ability spends one {R/G} and produces two mana in the chosen combination")
    void filterProducesTwoChosenMana() {
        String[][] combos = {{"RED", "RED"}, {"RED", "GREEN"}, {"GREEN", "GREEN"}};
        for (String[] combo : combos) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            harness.addToBattlefield(player1, new FireLitThicket());
            harness.addMana(player1, ManaColor.RED, 1); // pays the {R/G} activation cost
            GameData gd = harness.getGameData();

            harness.activateAbility(player1, 0, 1, null, null);
            harness.handleListChoice(player1, combo[0]);
            harness.handleListChoice(player1, combo[1]);

            int expectedRed = (combo[0].equals("RED") ? 1 : 0) + (combo[1].equals("RED") ? 1 : 0);
            int expectedGreen = 2 - expectedRed;
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(expectedRed);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(expectedGreen);
            assertThat(gd.stack).isEmpty();
        }
    }

    @Test
    @DisplayName("Filter ability cannot be activated without an {R/G} mana to pay")
    void filterRequiresManaCost() {
        harness.addToBattlefield(player1, new FireLitThicket());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Green mana pays the hybrid cost and can be converted to two red mana")
    void filterCanBePaidWithGreen() {
        var thicket = harness.addToBattlefieldAndReturn(player1, new FireLitThicket());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(thicket.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"COLORLESS", "WHITE", "BLUE", "BLACK"})
    @DisplayName("Mana other than red or green cannot pay the hybrid activation cost")
    void filterRejectsOtherMana(ManaColor color) {
        var thicket = harness.addToBattlefieldAndReturn(player1, new FireLitThicket());
        harness.addMana(player1, color, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(thicket.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Neither mana ability can be activated after the land has tapped for mana")
    void tappedLandCannotActivateAgain(int abilityIndex) {
        var thicket = harness.addToBattlefieldAndReturn(player1, new FireLitThicket());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(thicket.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
