package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WindstormDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control with flying get +1/+0")
    void boostsOtherOwnCreaturesWithFlying() {
        harness.addToBattlefield(player1, new WindstormDrake());
        harness.addToBattlefield(player1, new CloudSprite());

        Permanent sprite = findPermanent(player1, "Cloud Sprite");

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sprite)).isEqualTo(1);
    }

    @Test
    @DisplayName("Windstorm Drake does not boost itself")
    void doesNotBoostItself() {
        WindstormDrake card = new WindstormDrake();
        card.setPower(10);
        card.setToughness(10);
        harness.addToBattlefield(player1, card);

        Permanent drake = findPermanent(player1, "Windstorm Drake");

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(10);
    }

    @Test
    @DisplayName("Nonflying creatures and opponents' flying creatures are unaffected")
    void onlyBoostsOtherOwnCreaturesWithFlying() {
        harness.addToBattlefield(player1, new WindstormDrake());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new CloudSprite());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent opponentSprite = findPermanent(player2, "Cloud Sprite");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentSprite)).isEqualTo(1);
    }
}
