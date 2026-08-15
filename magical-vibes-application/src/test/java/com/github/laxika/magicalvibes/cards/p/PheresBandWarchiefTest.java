package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PheresBandWarchiefTest extends BaseCardTest {

    @Test
    @DisplayName("Other Centaur creatures you control get +1/+1 and vigilance and trample")
    void buffsOtherCentaurCreaturesYouControl() {
        harness.addToBattlefield(player1, new PheresBandWarchief());
        harness.addToBattlefield(player1, new PheresBandThunderhoof());

        Permanent thunderhoof = findPermanent(player1, "Pheres-Band Thunderhoof");

        assertThat(gqs.getEffectivePower(gd, thunderhoof)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, thunderhoof)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, thunderhoof, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, thunderhoof, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Pheres-Band Warchief does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new PheresBandWarchief());

        Permanent warchief = findPermanent(player1, "Pheres-Band Warchief");

        assertThat(gqs.getEffectivePower(gd, warchief)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warchief)).isEqualTo(3);
    }

    @Test
    @DisplayName("Pheres-Band Warchief does not buff non-Centaurs")
    void doesNotBuffNonCentaurs() {
        harness.addToBattlefield(player1, new PheresBandWarchief());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Pheres-Band Warchief does not buff an opponent's Centaurs")
    void doesNotBuffOpponentsCentaurs() {
        harness.addToBattlefield(player1, new PheresBandWarchief());
        harness.addToBattlefield(player2, new PheresBandThunderhoof());

        Permanent thunderhoof = findPermanent(player2, "Pheres-Band Thunderhoof");

        assertThat(gqs.getEffectivePower(gd, thunderhoof)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, thunderhoof)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, thunderhoof, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, thunderhoof, Keyword.TRAMPLE)).isFalse();
    }
}
