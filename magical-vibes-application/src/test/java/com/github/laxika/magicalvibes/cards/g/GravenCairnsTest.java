package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(GravenCairns.class)
class GravenCairnsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for {C} adds one colorless mana without using the stack")
    void tapForColorless() {
        harness.addToBattlefield(player1, new GravenCairns());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Filter ability spends one {B/R} and produces two mana in the chosen combination")
    void filterProducesTwoChosenMana() {
        String[][] combos = {{"BLACK", "BLACK"}, {"BLACK", "RED"}, {"RED", "RED"}};
        for (String[] combo : combos) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            harness.addToBattlefield(player1, new GravenCairns());
            harness.addMana(player1, ManaColor.BLACK, 1); // pays the {B/R} activation cost
            GameData gd = harness.getGameData();

            harness.activateAbility(player1, 0, 1, null, null);
            harness.handleListChoice(player1, combo[0]);
            harness.handleListChoice(player1, combo[1]);

            int expectedBlack = (combo[0].equals("BLACK") ? 1 : 0) + (combo[1].equals("BLACK") ? 1 : 0);
            int expectedRed = 2 - expectedBlack;
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(expectedBlack);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(expectedRed);
            assertThat(gd.stack).isEmpty();
        }
    }

    @Test
    @DisplayName("Filter ability can pay its hybrid cost with red mana")
    void filterCanPayHybridCostWithRedMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new GravenCairns());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Filter ability cannot be activated without a {B/R} mana to pay")
    void filterRequiresManaCost() {
        harness.addToBattlefield(player1, new GravenCairns());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Filter ability cannot use colorless mana to pay its hybrid cost")
    void filterRejectsColorlessManaForHybridCost() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new GravenCairns());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
