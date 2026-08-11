package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AngelOfVitalityTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 at 25 life and not below 25")
    void thresholdBoost() {
        harness.addToBattlefield(player1, new AngelOfVitality());
        Permanent angel = findAngel();

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(2);

        harness.setLife(player1, 25);
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);

        harness.setLife(player1, 24);
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds one life to each positive life-gain event")
    void addsOneLifeToGainEvent() {
        harness.addToBattlefield(player1, new AngelOfVitality());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Two Angels add two life to one life-gain event")
    void multipleAngelsStackAdditively() {
        harness.addToBattlefield(player1, new AngelOfVitality());
        harness.addToBattlefield(player1, new AngelOfVitality());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("A life gain by the opponent is not increased")
    void doesNotModifyOpponentsLifeGain() {
        harness.addToBattlefield(player1, new AngelOfVitality());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));

        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Increasing a life total is also modified")
    void modifiesLifeTotalIncrease() {
        harness.addToBattlefield(player1, new AngelOfVitality());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applySetLifeTotal(gd, player1.getId(), 23));

        harness.assertLife(player1, 24);
    }

    private Permanent findAngel() {
        return findPermanent(player1, "Angel of Vitality");
    }
}
