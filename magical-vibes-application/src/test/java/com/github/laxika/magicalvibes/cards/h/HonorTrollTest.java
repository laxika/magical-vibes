package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HonorTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+1 at 25 life and loses it below 25")
    void thresholdBoost() {
        harness.addToBattlefield(player1, new HonorTroll());
        Permanent troll = findTroll();

        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(3);

        harness.setLife(player1, 25);
        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(4);

        harness.setLife(player1, 24);
        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(3);
    }

    @Test
    @DisplayName("Adds one life to each positive life-gain event")
    void addsOneLifeToGainEvent() {
        harness.addToBattlefield(player1, new HonorTroll());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Does not modify the opponent's life gain")
    void doesNotModifyOpponentsLifeGain() {
        harness.addToBattlefield(player1, new HonorTroll());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));

        harness.assertLife(player2, 23);
    }

    private Permanent findTroll() {
        return findPermanent(player1, "Honor Troll");
    }
}
