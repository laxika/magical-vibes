package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AkkiUnderlingTest extends BaseCardTest {

    @Test
    void staysAtBaseStatsAndLacksFirstStrikeBelowThreshold() {
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addToBattlefield(player1, new AkkiUnderling());

        Permanent underling = findUnderling();
        assertThat(gqs.getEffectivePower(gd, underling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, underling)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, underling, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void getsBoostAndFirstStrikeAtSevenCardsInHand() {
        harness.setHand(player1, List.of(
                new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addToBattlefield(player1, new AkkiUnderling());

        Permanent underling = findUnderling();
        assertThat(gqs.getEffectivePower(gd, underling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, underling)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, underling, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void losesBoostAndFirstStrikeWhenHandDropsBelowThreshold() {
        harness.setHand(player1, List.of(
                new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addToBattlefield(player1, new AkkiUnderling());

        Permanent underling = findUnderling();
        assertThat(gqs.hasKeyword(gd, underling, Keyword.FIRST_STRIKE)).isTrue();

        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock()));

        assertThat(gqs.getEffectivePower(gd, underling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, underling)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, underling, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent findUnderling() {
        return findPermanent(player1, "Akki Underling");
    }
}
