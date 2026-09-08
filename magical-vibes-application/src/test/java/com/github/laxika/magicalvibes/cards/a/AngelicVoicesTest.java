package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AngelicVoicesTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your creatures when you control no nonartifact, nonwhite creatures")
    void boostsWithOnlyWhiteCreature() {
        harness.addToBattlefield(player1, new AngelicVoices());
        Permanent lions = harness.addToBattlefieldAndReturn(player1, new SavannahLions());

        assertThat(gqs.getEffectivePower(gd, lions)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lions)).isEqualTo(2);
    }

    @Test
    @DisplayName("A nonartifact, nonwhite creature disables the boost")
    void nonArtifactNonwhiteCreatureDisablesBoost() {
        harness.addToBattlefield(player1, new AngelicVoices());
        Permanent lions = harness.addToBattlefieldAndReturn(player1, new SavannahLions());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, lions)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lions)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("An artifact creature does not disable the boost")
    void artifactCreatureDoesNotDisableBoost() {
        harness.addToBattlefield(player1, new AngelicVoices());
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent lions = harness.addToBattlefieldAndReturn(player1, new SavannahLions());

        assertThat(gqs.getEffectivePower(gd, lions)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lions)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's nonartifact, nonwhite creature does not disable the boost")
    void opponentCreatureDoesNotDisableBoost() {
        harness.addToBattlefield(player1, new AngelicVoices());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent lions = harness.addToBattlefieldAndReturn(player1, new SavannahLions());

        assertThat(gqs.getEffectivePower(gd, lions)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lions)).isEqualTo(2);
    }
}
