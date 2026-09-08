package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeathmaskNezumiTest extends BaseCardTest {

    @Test
    @DisplayName("Has base stats and no fear with fewer than seven cards in hand")
    void belowThreshold() {
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock()));
        Permanent nezumi = addNezumi();

        assertThat(gqs.getEffectivePower(gd, nezumi)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nezumi)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nezumi, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Gets +2/+1 and fear with seven cards in hand")
    void thresholdActive() {
        harness.setHand(player1, List.of(
                new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock()));
        Permanent nezumi = addNezumi();

        assertThat(gqs.getEffectivePower(gd, nezumi)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nezumi)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, nezumi, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Gains and loses the bonus as hand size crosses seven")
    void tracksHandSize() {
        harness.setHand(player1, List.of(
                new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock()));
        Permanent nezumi = addNezumi();

        assertThat(gqs.getEffectivePower(gd, nezumi)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nezumi, Keyword.FEAR)).isFalse();

        harness.setHand(player1, List.of(
                new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock()));
        assertThat(gqs.getEffectivePower(gd, nezumi)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nezumi)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, nezumi, Keyword.FEAR)).isTrue();

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        assertThat(gqs.getEffectivePower(gd, nezumi)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nezumi)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nezumi, Keyword.FEAR)).isFalse();
    }

    private Permanent addNezumi() {
        return harness.addToBattlefieldAndReturn(player1, new DeathmaskNezumi());
    }
}
