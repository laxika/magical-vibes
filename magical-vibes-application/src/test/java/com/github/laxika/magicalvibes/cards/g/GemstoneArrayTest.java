package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(GemstoneArray.class)
class GemstoneArrayTest extends BaseCardTest {

    @Test
    @DisplayName("Paying two mana puts a charge counter on Gemstone Array")
    void payingTwoManaAddsChargeCounter() {
        Permanent array = harness.addToBattlefieldAndReturn(player1, new GemstoneArray());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(array.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing a charge counter adds one mana of the chosen color")
    void removingChargeCounterAddsMana() {
        Permanent array = harness.addToBattlefieldAndReturn(player1, new GemstoneArray());
        array.setCounterCount(CounterType.CHARGE, 1);
        GameData gameData = harness.getGameData();
        int manaBefore = gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(array.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(manaBefore + 1);
    }

    @Test
    @DisplayName("The mana ability cannot be activated without a charge counter")
    void cannotRemoveMissingChargeCounter() {
        Permanent array = harness.addToBattlefieldAndReturn(player1, new GemstoneArray());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The charge-counter ability cannot be activated without two mana")
    void cannotAddChargeCounterWithoutTwoMana() {
        Permanent array = harness.addToBattlefieldAndReturn(player1, new GemstoneArray());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(array.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Both abilities can be activated without tapping Gemstone Array")
    void abilitiesDoNotRequireTapping() {
        Permanent array = harness.addToBattlefieldAndReturn(player1, new GemstoneArray());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(array.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(array.isTapped()).isFalse();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(array.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(array.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
