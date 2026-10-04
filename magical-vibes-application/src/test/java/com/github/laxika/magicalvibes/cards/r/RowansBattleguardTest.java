package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RowansBattleguard.class, RowanScholarOfSparks.class, GarrukWildspeaker.class})
class RowansBattleguardTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +3/+0 while you control a Rowan planeswalker")
    void getsBonusWhileControllerControlsRowanPlaneswalker() {
        Permanent battleguard = harness.addToBattlefieldAndReturn(player1, new RowansBattleguard());
        assertThat(gqs.getEffectivePower(gd, battleguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, battleguard)).isEqualTo(3);

        Permanent rowan = harness.addToBattlefieldAndReturn(player1, new RowanScholarOfSparks());
        rowan.setCounterCount(CounterType.LOYALTY, 3);

        assertThat(gqs.getEffectivePower(gd, battleguard)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, battleguard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not get the bonus from an opponent's Rowan planeswalker")
    void opponentRowanDoesNotEnableBonus() {
        Permanent battleguard = harness.addToBattlefieldAndReturn(player1, new RowansBattleguard());
        Permanent rowan = harness.addToBattlefieldAndReturn(player2, new RowanScholarOfSparks());
        rowan.setCounterCount(CounterType.LOYALTY, 3);

        assertThat(gqs.getEffectivePower(gd, battleguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, battleguard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not get the bonus from a non-Rowan planeswalker")
    void nonRowanPlaneswalkerDoesNotEnableBonus() {
        Permanent battleguard = harness.addToBattlefieldAndReturn(player1, new RowansBattleguard());
        Permanent garruk = harness.addToBattlefieldAndReturn(player1, new GarrukWildspeaker());
        garruk.setCounterCount(CounterType.LOYALTY, 3);

        assertThat(gqs.getEffectivePower(gd, battleguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, battleguard)).isEqualTo(3);
    }
}
