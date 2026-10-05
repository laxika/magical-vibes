package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PheresBandWarchief.class, PheresBandThunderhoof.class, GrizzlyBears.class})
class PheresBandWarchiefTest extends BaseCardTest {

    @Test
    @DisplayName("Other Centaur creatures you control get +1/+1 and vigilance and trample")
    void buffsOtherCentaurCreaturesYouControl() {
        harness.addToBattlefield(player1, new PheresBandWarchief());
        Permanent thunderhoof = harness.addToBattlefieldAndReturn(player1, new PheresBandThunderhoof());

        assertThat(gqs.getEffectivePower(gd, thunderhoof)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, thunderhoof)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, thunderhoof, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, thunderhoof, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Pheres-Band Warchief does not buff itself")
    void doesNotBuffItself() {
        Permanent warchief = harness.addToBattlefieldAndReturn(player1, new PheresBandWarchief());

        assertThat(gqs.getEffectivePower(gd, warchief)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warchief)).isEqualTo(3);
    }

    @Test
    @DisplayName("Pheres-Band Warchief does not buff non-Centaurs")
    void doesNotBuffNonCentaurs() {
        harness.addToBattlefield(player1, new PheresBandWarchief());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Pheres-Band Warchief does not buff an opponent's Centaurs")
    void doesNotBuffOpponentsCentaurs() {
        harness.addToBattlefield(player1, new PheresBandWarchief());
        Permanent thunderhoof = harness.addToBattlefieldAndReturn(player2, new PheresBandThunderhoof());

        assertThat(gqs.getEffectivePower(gd, thunderhoof)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, thunderhoof)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, thunderhoof, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, thunderhoof, Keyword.TRAMPLE)).isFalse();
    }
    @Test
    @DisplayName("Multiple Warchiefs buff each other and their bonuses stack on other Centaurs")
    void multipleWarchiefsStack() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PheresBandWarchief());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PheresBandWarchief());
        Permanent thunderhoof = harness.addToBattlefieldAndReturn(player1, new PheresBandThunderhoof());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, thunderhoof)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, thunderhoof)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, thunderhoof, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, thunderhoof, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The boost and granted keywords disappear when the Warchief leaves")
    void bonusEndsWhenSourceLeaves() {
        Permanent warchief = harness.addToBattlefieldAndReturn(player1, new PheresBandWarchief());
        Permanent thunderhoof = harness.addToBattlefieldAndReturn(player1, new PheresBandThunderhoof());

        assertThat(gqs.getEffectivePower(gd, thunderhoof)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, thunderhoof)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, thunderhoof, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, thunderhoof, Keyword.TRAMPLE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, warchief));

        assertThat(gqs.getEffectivePower(gd, thunderhoof)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, thunderhoof)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, thunderhoof, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, thunderhoof, Keyword.TRAMPLE)).isFalse();
    }
}
