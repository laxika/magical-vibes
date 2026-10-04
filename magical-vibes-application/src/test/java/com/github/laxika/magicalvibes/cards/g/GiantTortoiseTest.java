package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiantTortoise.class, LightningBolt.class})
class GiantTortoiseTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +0/+3 while untapped")
    void untappedGetsBoost() {
        Permanent tortoise = harness.addToBattlefieldAndReturn(player1, new GiantTortoise());

        assertThat(tortoise.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, tortoise)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tortoise)).isEqualTo(4);
    }

    @Test
    @DisplayName("A tortoise does not boost another tortoise")
    void boostOnlyAffectsSource() {
        Permanent untappedTortoise = harness.addToBattlefieldAndReturn(player1, new GiantTortoise());
        Permanent tappedTortoise = harness.addToBattlefieldAndReturn(player1, new GiantTortoise());
        tappedTortoise.tap();

        assertThat(gqs.getEffectivePower(gd, untappedTortoise)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, untappedTortoise)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, tappedTortoise)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tappedTortoise)).isEqualTo(1);
    }

    @Test
    @DisplayName("Loses the boost while tapped")
    void tappedNoBoost() {
        Permanent tortoise = harness.addToBattlefieldAndReturn(player1, new GiantTortoise());
        tortoise.tap();

        assertThat(gqs.getEffectivePower(gd, tortoise)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tortoise)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost is removed when it taps and restored when it untaps")
    void boostFollowsTapState() {
        Permanent tortoise = harness.addToBattlefieldAndReturn(player1, new GiantTortoise());
        assertThat(gqs.getEffectiveToughness(gd, tortoise)).isEqualTo(4);

        tortoise.tap();
        assertThat(gqs.getEffectiveToughness(gd, tortoise)).isEqualTo(1);

        tortoise.untap();
        assertThat(gqs.getEffectiveToughness(gd, tortoise)).isEqualTo(4);
    }

    @Test
    @DisplayName("Untapped tortoise survives three damage thanks to its toughness bonus")
    void untappedSurvivesThreeDamage() {
        Permanent tortoise = harness.addToBattlefieldAndReturn(player2, new GiantTortoise());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, tortoise.getId());

        harness.assertOnBattlefield(player2, "Giant Tortoise");
        harness.assertNotInGraveyard(player2, "Giant Tortoise");
    }

    @Test
    @DisplayName("Tapped tortoise dies to three damage without its toughness bonus")
    void tappedDiesToThreeDamage() {
        Permanent tortoise = harness.addToBattlefieldAndReturn(player2, new GiantTortoise());
        tortoise.tap();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, tortoise.getId());

        harness.assertNotOnBattlefield(player2, "Giant Tortoise");
        harness.assertInGraveyard(player2, "Giant Tortoise");
    }
}
