package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PyramidOfThePantheon.class})
class PyramidOfThePantheonTest extends BaseCardTest {

    @Test
    @DisplayName("Brick ability adds one mana of the chosen color and a brick counter")
    void brickAbilityAddsManaAndCounter() {
        Permanent pyramid = harness.addToBattlefieldAndReturn(player1, new PyramidOfThePantheon());
        GameData gd = harness.getGameData();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int before = gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(before + 1);
        assertThat(pyramid.getCounterCount(CounterType.BRICK)).isEqualTo(1);
        assertThat(pyramid.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Three-mana ability cannot be activated with fewer than three brick counters")
    void bigManaAbilityRequiresThreeBricks() {
        Permanent pyramid = harness.addToBattlefieldAndReturn(player1, new PyramidOfThePantheon());
        pyramid.setCounterCount(CounterType.BRICK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("brick counters");
    }

    @Test
    @DisplayName("Three-mana ability adds three mana of one chosen color once charged")
    void bigManaAbilityAddsThreeMana() {
        Permanent pyramid = harness.addToBattlefieldAndReturn(player1, new PyramidOfThePantheon());
        pyramid.setCounterCount(CounterType.BRICK, 3);
        GameData gd = harness.getGameData();
        int before = gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(before + 3);
        assertThat(pyramid.isTapped()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void chargedAbilityProducesOnlyTheChosenColorWithoutConsumingBricks(ManaColor color) {
        Permanent pyramid = harness.addToBattlefieldAndReturn(player1, new PyramidOfThePantheon());
        pyramid.setCounterCount(CounterType.BRICK, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());

        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor == color ? 3 : 0);
        }
        assertThat(pyramid.getCounterCount(CounterType.BRICK)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(pyramid.isTapped()).isTrue();
    }

    @Test
    void brickAbilityCanContinueChargingAfterThreeBricksAndPaysTwoMana() {
        Permanent pyramid = harness.addToBattlefieldAndReturn(player1, new PyramidOfThePantheon());
        pyramid.setCounterCount(CounterType.BRICK, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(pyramid.getCounterCount(CounterType.BRICK)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
        assertThat(pyramid.isTapped()).isTrue();
    }

    @Test
    void brickAbilityCannotActivateWithoutTwoMana() {
        Permanent pyramid = harness.addToBattlefieldAndReturn(player1, new PyramidOfThePantheon());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(pyramid.isTapped()).isFalse();
        assertThat(pyramid.getCounterCount(CounterType.BRICK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
