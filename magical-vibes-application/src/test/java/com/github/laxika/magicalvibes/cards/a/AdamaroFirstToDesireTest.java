package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdamaroFirstToDesireTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the largest opponent hand size")
    void powerAndToughnessEqualLargestOpponentHandSize() {
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest()));
        Permanent adamaro = harness.addToBattlefieldAndReturn(player1, new AdamaroFirstToDesire());

        assertThat(gqs.getEffectivePower(gd, adamaro)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, adamaro)).isEqualTo(3);
    }

    @Test
    @DisplayName("Updates when an opponent's hand size changes")
    void updatesWhenOpponentsHandSizeChanges() {
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        Permanent adamaro = harness.addToBattlefieldAndReturn(player1, new AdamaroFirstToDesire());

        assertThat(gqs.getEffectivePower(gd, adamaro)).isEqualTo(2);

        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, adamaro)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, adamaro)).isEqualTo(4);
    }

    @Test
    @DisplayName("Is 0/0 when opponents have no cards in hand")
    void isZeroZeroWithEmptyOpponentHands() {
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player2, List.of());
        Permanent adamaro = harness.addToBattlefieldAndReturn(player1, new AdamaroFirstToDesire());

        assertThat(gqs.getEffectivePower(gd, adamaro)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, adamaro)).isZero();
    }
}
