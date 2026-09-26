package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.InnerChamberGuard;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathmaskNezumi.class, InnerChamberGuard.class})
class DeathmaskNezumiTest extends BaseCardTest {

    @Test
    @DisplayName("Has base stats and no fear with fewer than seven cards in hand")
    void belowThreshold() {
        harness.setHand(player1, List.of(
                new InnerChamberGuard(), new InnerChamberGuard(), new InnerChamberGuard(),
                new InnerChamberGuard(), new InnerChamberGuard(), new InnerChamberGuard()));
        Permanent nezumi = addNezumi();

        assertThat(gqs.getEffectivePower(gd, nezumi)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nezumi)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nezumi, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Gets +2/+1 and fear with seven cards in hand")
    void thresholdActive() {
        harness.setHand(player1, List.of(
                new InnerChamberGuard(), new InnerChamberGuard(), new InnerChamberGuard(),
                new InnerChamberGuard(), new InnerChamberGuard(), new InnerChamberGuard(),
                new InnerChamberGuard()));
        Permanent nezumi = addNezumi();

        assertThat(gqs.getEffectivePower(gd, nezumi)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nezumi)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, nezumi, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Gains and loses the bonus as hand size crosses seven")
    void tracksHandSize() {
        harness.setHand(player1, List.of(
                new InnerChamberGuard(), new InnerChamberGuard(), new InnerChamberGuard(),
                new InnerChamberGuard(), new InnerChamberGuard(), new InnerChamberGuard()));
        Permanent nezumi = addNezumi();

        assertThat(gqs.getEffectivePower(gd, nezumi)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nezumi, Keyword.FEAR)).isFalse();

        harness.setHand(player1, List.of(
                new InnerChamberGuard(), new InnerChamberGuard(), new InnerChamberGuard(),
                new InnerChamberGuard(), new InnerChamberGuard(), new InnerChamberGuard(),
                new InnerChamberGuard()));
        assertThat(gqs.getEffectivePower(gd, nezumi)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nezumi)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, nezumi, Keyword.FEAR)).isTrue();

        harness.setHand(player1, List.of(new InnerChamberGuard(), new InnerChamberGuard()));
        assertThat(gqs.getEffectivePower(gd, nezumi)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nezumi)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nezumi, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Uses the controller's hand size rather than the opponent's")
    void opponentHandDoesNotEnableBonus() {
        harness.setHand(player1, List.of(
                new InnerChamberGuard(), new InnerChamberGuard(), new InnerChamberGuard(),
                new InnerChamberGuard(), new InnerChamberGuard(), new InnerChamberGuard()));
        harness.setHand(player2, List.of(
                new InnerChamberGuard(), new InnerChamberGuard(), new InnerChamberGuard(),
                new InnerChamberGuard(), new InnerChamberGuard(), new InnerChamberGuard(),
                new InnerChamberGuard()));
        Permanent nezumi = addNezumi();

        assertThat(gqs.getEffectivePower(gd, nezumi)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nezumi)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nezumi, Keyword.FEAR)).isFalse();
    }

    private Permanent addNezumi() {
        return harness.addToBattlefieldAndReturn(player1, new DeathmaskNezumi());
    }
}
