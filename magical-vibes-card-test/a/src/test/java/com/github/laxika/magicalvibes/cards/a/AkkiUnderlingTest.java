package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkkiUnderling.class})
class AkkiUnderlingTest extends BaseCardTest {

    @Test
    void staysAtBaseStatsAndLacksFirstStrikeBelowThreshold() {
        harness.setHand(player1, List.of(
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(),
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling()));
        Permanent underling = harness.addToBattlefieldAndReturn(player1, new AkkiUnderling());
        assertThat(gqs.getEffectivePower(gd, underling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, underling)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, underling, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void onlyCountsTheControllersHand() {
        harness.setHand(player1, List.of(
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(),
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling()));
        harness.setHand(player2, List.of(
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(),
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling()));
        Permanent underling = harness.addToBattlefieldAndReturn(player1, new AkkiUnderling());
        assertThat(gqs.getEffectivePower(gd, underling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, underling)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, underling, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void getsBoostAndFirstStrikeAtSevenCardsInHand() {
        harness.setHand(player1, List.of(
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(),
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling()));
        Permanent underling = harness.addToBattlefieldAndReturn(player1, new AkkiUnderling());
        assertThat(gqs.getEffectivePower(gd, underling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, underling)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, underling, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void losesBoostAndFirstStrikeWhenHandDropsBelowThreshold() {
        harness.setHand(player1, List.of(
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(),
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling()));
        Permanent underling = harness.addToBattlefieldAndReturn(player1, new AkkiUnderling());
        assertThat(gqs.hasKeyword(gd, underling, Keyword.FIRST_STRIKE)).isTrue();

        harness.setHand(player1, List.of(
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(),
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling()));

        assertThat(gqs.getEffectivePower(gd, underling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, underling)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, underling, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void gainsBoostAndFirstStrikeWhenHandReachesThreshold() {
        harness.setHand(player1, List.of(
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(),
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling()));
        Permanent underling = harness.addToBattlefieldAndReturn(player1, new AkkiUnderling());

        assertThat(gqs.getEffectivePower(gd, underling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, underling)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, underling, Keyword.FIRST_STRIKE)).isFalse();

        harness.setHand(player1, List.of(
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(),
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling()));

        assertThat(gqs.getEffectivePower(gd, underling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, underling)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, underling, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void bonusRemainsAboveSevenCardsAndEachUnderlingOnlyBoostsItself() {
        harness.setHand(player1, List.of(
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(),
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling()));
        harness.setHand(player2, List.of());
        Permanent underling = harness.addToBattlefieldAndReturn(player1, new AkkiUnderling());
        Permanent secondUnderling = harness.addToBattlefieldAndReturn(player1, new AkkiUnderling());
        Permanent opposingUnderling = harness.addToBattlefieldAndReturn(player2, new AkkiUnderling());

        assertThat(gqs.getEffectivePower(gd, underling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, underling)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, underling, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, secondUnderling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondUnderling)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, secondUnderling, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingUnderling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingUnderling)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opposingUnderling, Keyword.FIRST_STRIKE)).isFalse();
    }
}
