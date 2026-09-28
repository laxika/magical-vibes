package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiamondCity.class, GrizzlyBears.class})
class DiamondCityTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a shield counter")
    void entersWithShieldCounter() {
        Permanent city = harness.enterBattlefieldAndReturn(player1, new DiamondCity());

        assertThat(city.getCounterCount(CounterType.SHIELD)).isOne();
    }

    @Test
    @DisplayName("Tapping produces one colorless mana")
    void tapsForColorlessMana() {
        Permanent city = harness.addToBattlefieldAndReturn(player1, new DiamondCity());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isOne();
        assertThat(city.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Moves its shield counter to any target creature after two creatures enter")
    void movesShieldCounterAfterTwoCreaturesEnter() {
        Permanent city = harness.enterBattlefieldAndReturn(player1, new DiamondCity());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(city.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(target.getCounterCount(CounterType.SHIELD)).isOne();
    }

    @Test
    @DisplayName("Cannot move a shield counter before two creatures enter")
    void cannotActivateBeforeTwoCreaturesEnter() {
        Permanent city = harness.enterBattlefieldAndReturn(player1, new DiamondCity());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
