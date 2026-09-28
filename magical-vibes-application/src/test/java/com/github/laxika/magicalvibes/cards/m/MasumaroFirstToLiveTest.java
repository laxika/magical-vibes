package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HandOfHonor;
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
}
