package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.f.ForsakenWastes;
import com.github.laxika.magicalvibes.cards.w.WildElephant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RadiantEssence.class, FeralShadow.class, ForsakenWastes.class, WildElephant.class})
class RadiantEssenceTest extends BaseCardTest {

    @Test
    @DisplayName("Base 2/3 when no opponent controls a black permanent")
    void baseWithoutBlackPermanent() {
        harness.addToBattlefield(player1, new RadiantEssence());

        Permanent essence = findPermanent(player1, "Radiant Essence");
        assertThat(gqs.getEffectivePower(gd, essence)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, essence)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+2 (3/5) when an opponent controls a black permanent")
    void boostWhenOpponentControlsBlackPermanent() {
        harness.addToBattlefield(player1, new RadiantEssence());
        harness.addToBattlefield(player2, new FeralShadow());

        Permanent essence = findPermanent(player1, "Radiant Essence");
        assertThat(gqs.getEffectivePower(gd, essence)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, essence)).isEqualTo(5);
    }

    @Test
    @DisplayName("No boost when the opponent's permanent is not black")
    void noBoostWhenPermanentNotBlack() {
        harness.addToBattlefield(player1, new RadiantEssence());
        harness.addToBattlefield(player2, new WildElephant());

        Permanent essence = findPermanent(player1, "Radiant Essence");
        assertThat(gqs.getEffectivePower(gd, essence)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, essence)).isEqualTo(3);
    }

    @Test
    @DisplayName("The controller's own black permanent does not grant the boost")
    void noBoostFromOwnBlackPermanent() {
        harness.addToBattlefield(player1, new RadiantEssence());
        harness.addToBattlefield(player1, new FeralShadow());

        Permanent essence = findPermanent(player1, "Radiant Essence");
        assertThat(gqs.getEffectivePower(gd, essence)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, essence)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost turns on when an opponent's black permanent enters")
    void boostUpdatesWhenOpponentControlsBlackPermanent() {
        harness.addToBattlefield(player1, new RadiantEssence());

        Permanent essence = findPermanent(player1, "Radiant Essence");
        assertThat(gqs.getEffectivePower(gd, essence)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, essence)).isEqualTo(3);

        harness.addToBattlefield(player2, new FeralShadow());

        assertThat(gqs.getEffectivePower(gd, essence)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, essence)).isEqualTo(5);
    }

    @Test
    @DisplayName("An opponent's black noncreature permanent grants the boost")
    void boostFromBlackEnchantment() {
        Permanent essence = harness.addToBattlefieldAndReturn(player1, new RadiantEssence());
        harness.addToBattlefield(player2, new ForsakenWastes());

        assertThat(gqs.getEffectivePower(gd, essence)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, essence)).isEqualTo(5);
    }

    @Test
    @DisplayName("Multiple black permanents grant only one boost, which ends when the last leaves")
    void boostTracksLastBlackPermanentLeaving() {
        Permanent essence = harness.addToBattlefieldAndReturn(player1, new RadiantEssence());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new FeralShadow());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FeralShadow());
        harness.addToBattlefield(player1, new FeralShadow());

        assertThat(gqs.getEffectivePower(gd, essence)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, essence)).isEqualTo(5);

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);

        assertThat(gqs.getEffectivePower(gd, essence)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, essence)).isEqualTo(5);

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, second);

        assertThat(gqs.getEffectivePower(gd, essence)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, essence)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost applies only to Radiant Essence")
    void doesNotBoostOtherCreatures() {
        harness.addToBattlefield(player1, new RadiantEssence());
        Permanent elephant = harness.addToBattlefieldAndReturn(player1, new WildElephant());
        Permanent shadow = harness.addToBattlefieldAndReturn(player2, new FeralShadow());

        assertThat(gqs.getEffectivePower(gd, elephant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elephant)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, shadow)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shadow)).isEqualTo(1);
    }
}
