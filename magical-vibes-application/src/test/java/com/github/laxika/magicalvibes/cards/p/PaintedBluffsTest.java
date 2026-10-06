package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PaintedBluffs.class})
class PaintedBluffsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for {C} adds one colorless mana without using the stack")
    void tapForColorless() {
        harness.addToBattlefield(player1, new PaintedBluffs());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Filter ability spends {1} and adds one mana of the chosen color")
    void filterAddsChosenColor() {
        harness.addToBattlefield(player1, new PaintedBluffs());
        harness.addMana(player1, ManaColor.COLORLESS, 1); // pays the {1} activation cost
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.get(ManaColor.COLORLESS)).isZero(); // {1} was consumed as the cost
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Filter ability cannot be activated without {1} to pay")
    void filterRequiresManaCost() {
        harness.addToBattlefield(player1, new PaintedBluffs());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Filter can produce each color and accepts colored mana for its generic cost")
    void filterProducesEachColorWithColoredPayment(ManaColor color) {
        var bluffs = harness.addToBattlefieldAndReturn(player1, new PaintedBluffs());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(bluffs.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();

        harness.handleListChoice(player1, color.name());

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(color)).isEqualTo(1);
        assertThat(pool.getTotal()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Neither mana ability can be activated when Painted Bluffs is tapped")
    void tappedLandCannotActivate(int abilityIndex) {
        var bluffs = harness.addToBattlefieldAndReturn(player1, new PaintedBluffs());
        bluffs.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bluffs.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Failed filter activation leaves Painted Bluffs untapped and usable for colorless mana")
    void failedFilterDoesNotConsumeTapCost() {
        var bluffs = harness.addToBattlefieldAndReturn(player1, new PaintedBluffs());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bluffs.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(bluffs.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
