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
        harness.addToBattlefield(player1, new GemstoneArray());

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

    @Test
    @DisplayName("Adding a charge counter uses the stack and pays mana immediately")
    void chargeCounterIsAddedOnlyOnResolution() {
        Permanent array = harness.addToBattlefieldAndReturn(player1, new GemstoneArray());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(array.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();

        assertThat(array.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped Gemstone Array can use both abilities")
    void tappedArrayCanUseBothAbilities() {
        Permanent array = harness.addToBattlefieldAndReturn(player1, new GemstoneArray());
        array.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(array.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.stack).isEmpty();

        harness.handleListChoice(player1, "RED");

        assertThat(array.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each charge counter can produce a different color without using the stack")
    void producesEveryColorOneCounterAtATime() {
        Permanent array = harness.addToBattlefieldAndReturn(player1, new GemstoneArray());
        array.setCounterCount(CounterType.CHARGE, 5);
        ManaColor[] colors = {ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK, ManaColor.RED, ManaColor.GREEN};

        for (int i = 0; i < colors.length; i++) {
            harness.activateAbility(player1, 0, 1, null, null);

            assertThat(array.getCounterCount(CounterType.CHARGE)).isEqualTo(4 - i);
            assertThat(gd.stack).isEmpty();

            harness.handleListChoice(player1, colors[i].name());

            assertThat(gd.playerManaPools.get(player1.getId()).get(colors[i])).isEqualTo(1);
            assertThat(gd.stack).isEmpty();
        }

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
