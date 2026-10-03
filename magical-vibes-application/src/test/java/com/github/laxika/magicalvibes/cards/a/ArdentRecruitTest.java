package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArdentRecruit.class, IchorWellspring.class})
class ArdentRecruitTest extends BaseCardTest {

    @Test
    @DisplayName("Base 1/1 without metalcraft")
    void noMetalcraftBaseStats() {
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new ArdentRecruit());

        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, recruit)).isEqualTo(1);
    }

    @Test
    @DisplayName("Still 1/1 with only two artifacts")
    void noMetalcraftWithTwoArtifacts() {
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new ArdentRecruit());
        harness.addToBattlefield(player1, new IchorWellspring());
        harness.addToBattlefield(player1, new IchorWellspring());

        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, recruit)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +2/+2 with three artifacts becoming 3/3")
    void metalcraftWithThreeArtifacts() {
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new ArdentRecruit());
        harness.addToBattlefield(player1, new IchorWellspring());
        harness.addToBattlefield(player1, new IchorWellspring());
        harness.addToBattlefield(player1, new IchorWellspring());

        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recruit)).isEqualTo(3);
    }

    @Test
    @DisplayName("Loses boost when artifact count drops below three")
    void losesMetalcraftWhenArtifactRemoved() {
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new ArdentRecruit());
        harness.addToBattlefield(player1, new IchorWellspring());
        harness.addToBattlefield(player1, new IchorWellspring());
        harness.addToBattlefield(player1, new IchorWellspring());

        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(3);

        // Remove one artifact, leaving two.
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Ichor Wellspring"));

        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, recruit)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new ArdentRecruit());
        harness.addToBattlefield(player2, new IchorWellspring());
        harness.addToBattlefield(player2, new IchorWellspring());
        harness.addToBattlefield(player2, new IchorWellspring());

        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, recruit)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gains metalcraft immediately when the third artifact enters")
    void gainsMetalcraftAfterEntering() {
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new ArdentRecruit());
        harness.addToBattlefield(player1, new IchorWellspring());
        harness.addToBattlefield(player1, new IchorWellspring());

        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, recruit)).isEqualTo(1);

        harness.addToBattlefield(player1, new IchorWellspring());

        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recruit)).isEqualTo(3);
    }

    @Test
    @DisplayName("Four artifacts grant only one bonus and do not boost the opponent's Recruit")
    void extraArtifactsDoNotStackOrBoostOpponent() {
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new ArdentRecruit());
        Permanent opponentRecruit = harness.addToBattlefieldAndReturn(player2, new ArdentRecruit());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new IchorWellspring());
        }

        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recruit)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentRecruit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentRecruit)).isEqualTo(1);
    }

}
