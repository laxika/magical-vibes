package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecretkeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 and flying when its controller has more cards in hand")
    void getsBoostWhenControllerHasMoreCardsInHand() {
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        Permanent secretkeeper = harness.addToBattlefieldAndReturn(player1, new Secretkeeper());

        assertThat(gqs.getEffectivePower(gd, secretkeeper)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secretkeeper)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, secretkeeper, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not get the bonus when hand sizes are tied")
    void noBoostWhenHandSizesAreTied() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        Permanent secretkeeper = harness.addToBattlefieldAndReturn(player1, new Secretkeeper());

        assertThat(gqs.getEffectivePower(gd, secretkeeper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secretkeeper)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, secretkeeper, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Loses the bonus when an opponent has more cards in hand")
    void noBoostWhenOpponentHasMoreCardsInHand() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        Permanent secretkeeper = harness.addToBattlefieldAndReturn(player1, new Secretkeeper());

        assertThat(gqs.getEffectivePower(gd, secretkeeper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secretkeeper)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, secretkeeper, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Updates when hand sizes change")
    void updatesWhenHandSizesChange() {
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        Permanent secretkeeper = harness.addToBattlefieldAndReturn(player1, new Secretkeeper());

        assertThat(gqs.getEffectivePower(gd, secretkeeper)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, secretkeeper, Keyword.FLYING)).isTrue();

        harness.setHand(player1, List.of(new Forest()));
        assertThat(gqs.getEffectivePower(gd, secretkeeper)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, secretkeeper, Keyword.FLYING)).isFalse();
    }
}
