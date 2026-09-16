package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AncestralTribute;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysticZealot.class, AncestralTribute.class})
class MysticZealotTest extends BaseCardTest {

    @Test
    @DisplayName("Has no threshold bonus with fewer than seven cards in its controller's graveyard")
    void noThresholdBonus() {
        harness.setGraveyard(player1, List.of(
                new AncestralTribute(), new AncestralTribute(), new AncestralTribute(),
                new AncestralTribute(), new AncestralTribute(), new AncestralTribute()));

        Permanent zealot = harness.addToBattlefieldAndReturn(player1, new MysticZealot());
        assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zealot)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, zealot, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+1 and flying with seven cards in its controller's graveyard")
    void thresholdBonus() {
        harness.setGraveyard(player1, graveyardWithSevenCards());

        Permanent zealot = harness.addToBattlefieldAndReturn(player1, new MysticZealot());
        assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zealot)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, zealot, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable threshold")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, graveyardWithSevenCards());

        Permanent zealot = harness.addToBattlefieldAndReturn(player1, new MysticZealot());
        assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zealot)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, zealot, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Loses the threshold bonus when its controller's graveyard drops below seven cards")
    void losesThresholdBonusWhenGraveyardChanges() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent zealot = harness.addToBattlefieldAndReturn(player1, new MysticZealot());

        assertThat(gqs.hasKeyword(gd, zealot, Keyword.FLYING)).isTrue();

        harness.setGraveyard(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zealot)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, zealot, Keyword.FLYING)).isFalse();
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new AncestralTribute(), new AncestralTribute(), new AncestralTribute(), new AncestralTribute(),
                new AncestralTribute(), new AncestralTribute(), new AncestralTribute());
    }
}
