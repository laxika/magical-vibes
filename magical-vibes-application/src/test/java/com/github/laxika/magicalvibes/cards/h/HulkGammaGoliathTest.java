package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AmaranthineWall;
import com.github.laxika.magicalvibes.cards.c.CaptainMarvelEarthsProtector;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HulkGammaGoliath.class, CaptainMarvelEarthsProtector.class, AmaranthineWall.class})
class HulkGammaGoliathTest extends BaseCardTest {

    @Test
    @DisplayName("Entry-turn Power-up puts five +1/+1 counters on Hulk")
    void powerUpIsDiscountedDuringEntryTurn() {
        Permanent hulk = harness.enterBattlefieldAndReturn(player1, new HulkGammaGoliath());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Reduces another creature's Power-up ability by three generic mana")
    void reducesOtherControlledCreaturePowerUp() {
        addCreatureReady(player1, new HulkGammaGoliath());
        Permanent captainMarvel = addCreatureReady(player1, new CaptainMarvelEarthsProtector());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(captainMarvel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(captainMarvel.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not reduce Hulk's own or non-Power-up abilities")
    void doesNotReduceOwnOrNonPowerUpAbilities() {
        addCreatureReady(player1, new HulkGammaGoliath());
        addCreatureReady(player1, new AmaranthineWall());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
