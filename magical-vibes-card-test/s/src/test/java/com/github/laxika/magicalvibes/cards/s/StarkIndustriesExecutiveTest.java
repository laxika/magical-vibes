package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(StarkIndustriesExecutive.class)
class StarkIndustriesExecutiveTest extends BaseCardTest {

    @Test
    @DisplayName("Pays two mana and taps to create a Treasure token")
    void paysManaAndTapsToCreateTreasure() {
        Permanent executive = addCreatureReady(player1, new StarkIndustriesExecutive());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(executive.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(countPermanents(player1, "Treasure")).isZero();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent executive = harness.addToBattlefieldAndReturn(player1, new StarkIndustriesExecutive());
        executive.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(executive.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent executive = addCreatureReady(player1, new StarkIndustriesExecutive());
        executive.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void cannotActivateWithOnlyOneMana() {
        Permanent executive = addCreatureReady(player1, new StarkIndustriesExecutive());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(executive.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void treasureCanImmediatelyBeSacrificedForAnyColor(ManaColor color) {
        addCreatureReady(player1, new StarkIndustriesExecutive());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
        assertThat(countPermanents(player2, "Treasure")).isZero();

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
