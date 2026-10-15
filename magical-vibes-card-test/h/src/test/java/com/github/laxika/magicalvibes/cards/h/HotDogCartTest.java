package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HotDogCart.class})
class HotDogCartTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token when it enters")
    void createsFoodTokenOnEnter() {
        harness.castFromHand(player1, new HotDogCart(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Food");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player2, "Food");
    }

    @Test
    @DisplayName("Tapping adds the chosen color of mana")
    void tappingAddsChosenColor() {
        harness.addToBattlefield(player1, new HotDogCart());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The created Food token can be sacrificed for life")
    void foodTokenCanBeSacrificed() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromHand(player1, new HotDogCart(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Mana resolves immediately and taps the Cart for each color")
    void manaAbilityResolvesImmediately(ManaColor color) {
        harness.addToBattlefield(player1, new HotDogCart());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
    }

    @Test
    @DisplayName("Food cannot be sacrificed without paying two mana")
    void foodRequiresTwoMana() {
        harness.castFromHand(player1, new HotDogCart(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }
}
