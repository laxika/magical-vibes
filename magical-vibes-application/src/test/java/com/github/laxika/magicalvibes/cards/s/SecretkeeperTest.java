package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Secretkeeper.class})
class SecretkeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 and flying when its controller has more cards in hand")
    void getsBoostWhenControllerHasMoreCardsInHand() {
        harness.setHand(player1, List.of(new Secretkeeper(), new Secretkeeper()));
        harness.setHand(player2, List.of(new Secretkeeper()));
        Permanent secretkeeper = harness.addToBattlefieldAndReturn(player1, new Secretkeeper());

        assertThat(gqs.getEffectivePower(gd, secretkeeper)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secretkeeper)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, secretkeeper, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not get the bonus when hand sizes are tied")
    void noBoostWhenHandSizesAreTied() {
        harness.setHand(player1, List.of(new Secretkeeper()));
        harness.setHand(player2, List.of(new Secretkeeper()));
        Permanent secretkeeper = harness.addToBattlefieldAndReturn(player1, new Secretkeeper());

        assertThat(gqs.getEffectivePower(gd, secretkeeper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secretkeeper)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, secretkeeper, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Loses the bonus when an opponent has more cards in hand")
    void noBoostWhenOpponentHasMoreCardsInHand() {
        harness.setHand(player1, List.of(new Secretkeeper()));
        harness.setHand(player2, List.of(new Secretkeeper(), new Secretkeeper()));
        Permanent secretkeeper = harness.addToBattlefieldAndReturn(player1, new Secretkeeper());

        assertThat(gqs.getEffectivePower(gd, secretkeeper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secretkeeper)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, secretkeeper, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Updates when hand sizes change")
    void updatesWhenHandSizesChange() {
        harness.setHand(player1, List.of(new Secretkeeper(), new Secretkeeper()));
        harness.setHand(player2, List.of(new Secretkeeper()));
        Permanent secretkeeper = harness.addToBattlefieldAndReturn(player1, new Secretkeeper());

        assertThat(gqs.getEffectivePower(gd, secretkeeper)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, secretkeeper, Keyword.FLYING)).isTrue();

        harness.setHand(player1, List.of(new Secretkeeper()));
        assertThat(gqs.getEffectivePower(gd, secretkeeper)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, secretkeeper, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Regains both bonuses when the opponent's hand shrinks")
    void regainsBonusWhenOpponentHandShrinks() {
        harness.setHand(player1, List.of(new Secretkeeper()));
        harness.setHand(player2, List.of(new Secretkeeper(), new Secretkeeper()));
        Permanent secretkeeper = harness.addToBattlefieldAndReturn(player1, new Secretkeeper());

        assertThat(gqs.getEffectivePower(gd, secretkeeper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secretkeeper)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, secretkeeper, Keyword.FLYING)).isFalse();

        harness.setHand(player2, List.of());

        assertThat(gqs.getEffectivePower(gd, secretkeeper)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secretkeeper)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, secretkeeper, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not get either bonus when both hands are empty")
    void noBonusWhenBothHandsAreEmpty() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Permanent secretkeeper = harness.addToBattlefieldAndReturn(player1, new Secretkeeper());

        assertThat(gqs.getEffectivePower(gd, secretkeeper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secretkeeper)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, secretkeeper, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Each Secretkeeper checks its own controller and only boosts itself")
    void eachSecretkeeperUsesItsOwnControllerHand() {
        harness.setHand(player1, List.of(new Secretkeeper()));
        harness.setHand(player2, List.of(new Secretkeeper(), new Secretkeeper()));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Secretkeeper());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Secretkeeper());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
    }
}
