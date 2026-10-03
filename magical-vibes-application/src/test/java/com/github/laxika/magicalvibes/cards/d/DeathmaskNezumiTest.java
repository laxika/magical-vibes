package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.InnerChamberGuard;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    @DisplayName("Fear prevents a white creature from blocking at seven cards")
    void fearPreventsWhiteBlocker() {
        harness.setHand(player1, java.util.stream.IntStream.range(0, 7)
                .mapToObj(i -> new InnerChamberGuard()).collect(java.util.stream.Collectors.toList()));
        Permanent attacker = addCreatureReady(player1, new DeathmaskNezumi());
        attacker.setAttacking(true);
        addCreatureReady(player2, new InnerChamberGuard());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fear");
    }

    @Test
    @DisplayName("A white creature can block after the hand drops below seven")
    void whiteCreatureCanBlockWhenBonusEnds() {
        harness.setHand(player1, java.util.stream.IntStream.range(0, 7)
                .mapToObj(i -> new InnerChamberGuard()).collect(java.util.stream.Collectors.toList()));
        Permanent attacker = addCreatureReady(player1, new DeathmaskNezumi());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new InnerChamberGuard());
        harness.setHand(player1, List.of());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("More than seven cards grants the bonus only to Deathmask Nezumi")
    void aboveThresholdDoesNotBoostOtherCreatures() {
        harness.setHand(player1, java.util.stream.IntStream.range(0, 8)
                .mapToObj(i -> new InnerChamberGuard()).collect(java.util.stream.Collectors.toList()));
        Permanent nezumi = addNezumi();
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new InnerChamberGuard());

        assertThat(gqs.getEffectivePower(gd, nezumi)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nezumi)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, nezumi, Keyword.FEAR)).isTrue();
        assertThat(gqs.getEffectivePower(gd, guard)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, guard, Keyword.FEAR)).isFalse();
    }

    private Permanent addNezumi() {
        return harness.addToBattlefieldAndReturn(player1, new DeathmaskNezumi());
    }
}
