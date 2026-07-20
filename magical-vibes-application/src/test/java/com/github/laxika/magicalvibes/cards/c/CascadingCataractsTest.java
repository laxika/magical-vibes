package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CascadingCataractsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for {C} adds one colorless mana without using the stack")
    void tapForColorless() {
        harness.addToBattlefield(player1, new CascadingCataracts());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Filter ability spends {5} and adds five mana in any combination of colors")
    void filterAddsFiveMixedColors() {
        harness.addToBattlefield(player1, new CascadingCataracts());
        harness.addMana(player1, ManaColor.COLORLESS, 5); // pays the {5} activation cost
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 1, null, null);
        // Each of the five mana is chosen independently — one of each color proves the combination.
        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.get(ManaColor.COLORLESS)).isZero(); // {5} was consumed as the cost
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Filter ability can add five mana of a single chosen color")
    void filterAddsFiveOfOneColor() {
        harness.addToBattlefield(player1, new CascadingCataracts());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 1, null, null);
        for (int i = 0; i < 5; i++) {
            harness.handleListChoice(player1, "BLUE");
        }

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Filter ability cannot be activated without {5} to pay")
    void filterRequiresManaCost() {
        harness.addToBattlefield(player1, new CascadingCataracts());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
