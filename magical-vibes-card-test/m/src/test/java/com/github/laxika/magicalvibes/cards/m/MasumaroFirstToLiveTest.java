package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HandOfHonor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MasumaroFirstToLive.class, HandOfHonor.class})
class MasumaroFirstToLiveTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal twice the controller's hand size")
    void powerAndToughnessEqualTwiceHandSize() {
        harness.setHand(player1, List.of(new HandOfHonor(), new HandOfHonor(), new HandOfHonor()));
        Permanent masumaro = harness.addToBattlefieldAndReturn(player1, new MasumaroFirstToLive());

        assertThat(gqs.getEffectivePower(gd, masumaro)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, masumaro)).isEqualTo(6);
    }

    @Test
    @DisplayName("Updates when the controller's hand size changes")
    void updatesWhenHandSizeChanges() {
        harness.setHand(player1, List.of(new HandOfHonor(), new HandOfHonor()));
        Permanent masumaro = harness.addToBattlefieldAndReturn(player1, new MasumaroFirstToLive());

        assertThat(gqs.getEffectivePower(gd, masumaro)).isEqualTo(4);

        harness.setHand(player1, List.of(
                new HandOfHonor(), new HandOfHonor(), new HandOfHonor(), new HandOfHonor()));

        assertThat(gqs.getEffectivePower(gd, masumaro)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, masumaro)).isEqualTo(8);
    }

    @Test
    @DisplayName("Counts only the controller's hand")
    void countsOnlyControllerHand() {
        harness.setHand(player1, List.of(new HandOfHonor(), new HandOfHonor()));
        harness.setHand(player2, List.of(
                new HandOfHonor(), new HandOfHonor(), new HandOfHonor(), new HandOfHonor()));
        Permanent masumaro = harness.addToBattlefieldAndReturn(player1, new MasumaroFirstToLive());

        assertThat(gqs.getEffectivePower(gd, masumaro)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, masumaro)).isEqualTo(4);
    }

    @Test
    @DisplayName("Is 0/0 with an empty controller's hand")
    void isZeroZeroWithEmptyControllerHand() {
        harness.setHand(player1, List.of());
        Permanent masumaro = harness.addToBattlefieldAndReturn(player1, new MasumaroFirstToLive());

        assertThat(gqs.getEffectivePower(gd, masumaro)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, masumaro)).isZero();
    }

    @Test
    @DisplayName("Counts itself while in its owner's hand")
    void countsItselfInHand() {
        MasumaroFirstToLive masumaro = new MasumaroFirstToLive();
        harness.setHand(player1, List.of(masumaro, new HandOfHonor()));
        harness.setHand(player2, List.of());

        assertThat(gqs.getEffectiveCardPower(gd, masumaro)).isEqualTo(4);
        assertThat(gqs.getEffectiveCardToughness(gd, masumaro)).isEqualTo(4);
    }

    @Test
    @DisplayName("Defines power and toughness in the graveyard using its owner's current hand")
    void definesPowerAndToughnessInGraveyard() {
        MasumaroFirstToLive masumaro = new MasumaroFirstToLive();
        harness.setGraveyard(player1, List.of(masumaro));
        harness.setHand(player1, List.of(new HandOfHonor(), new HandOfHonor()));
        harness.setHand(player2, List.of(new HandOfHonor()));

        assertThat(gqs.getEffectiveCardPower(gd, masumaro)).isEqualTo(4);
        assertThat(gqs.getEffectiveCardToughness(gd, masumaro)).isEqualTo(4);

        harness.setHand(player1, List.of());

        assertThat(gqs.getEffectiveCardPower(gd, masumaro)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, masumaro)).isZero();
    }

    @Test
    @DisplayName("Dies after resolving when cast as the last card in hand")
    void diesWhenCastAsLastCardInHand() {
        harness.castFromHand(player1, new MasumaroFirstToLive(), "{3}{G}{G}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Masumaro, First to Live");
        harness.assertInGraveyard(player1, "Masumaro, First to Live");
    }

    @Test
    @DisplayName("Dies when its controller's hand becomes empty")
    void diesWhenHandBecomesEmpty() {
        harness.setHand(player1, List.of(new HandOfHonor()));
        harness.addToBattlefield(player1, new MasumaroFirstToLive());
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player1, "Masumaro, First to Live");

        harness.setHand(player1, List.of());
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Masumaro, First to Live");
        harness.assertInGraveyard(player1, "Masumaro, First to Live");
    }

    @Test
    @DisplayName("Counters add to the defined power and toughness and can keep it alive with no cards")
    void countersAddToDefinedPowerAndToughness() {
        harness.setHand(player1, List.of(new HandOfHonor(), new HandOfHonor()));
        Permanent masumaro = harness.addToBattlefieldAndReturn(player1, new MasumaroFirstToLive());
        masumaro.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.getEffectivePower(gd, masumaro)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, masumaro)).isEqualTo(5);

        harness.setHand(player1, List.of());
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Masumaro, First to Live");
        assertThat(gqs.getEffectivePower(gd, masumaro)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, masumaro)).isEqualTo(1);
    }
}