package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.a.Archangel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.q.Quicksand;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YavimayaCradleOfGrowth.class, Quicksand.class, Archangel.class})
class YavimayaCradleOfGrowthTest extends BaseCardTest {

    @Test
    void allLandsGainForestSubtype() {
        harness.addToBattlefield(player1, new YavimayaCradleOfGrowth());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Quicksand());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Quicksand());

        assertThat(gqs.hasEffectiveSubtype(gd, ownLand, CardSubtype.FOREST)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, opponentLand, CardSubtype.FOREST)).isTrue();
    }

    @CardUsed(Forest.class)
    @Test
    void landRetainsItsOtherLandTypes() {
        harness.addToBattlefield(player1, new YavimayaCradleOfGrowth());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.hasEffectiveSubtype(gd, forest, CardSubtype.FOREST)).isTrue();
    }

    @Test
    void landsCanTapForGreen() {
        harness.addToBattlefield(player1, new YavimayaCradleOfGrowth());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Quicksand());

        // Quicksand has two printed activated abilities; the granted Forest ability is index 2.
        harness.activateAbility(player1, 1, 2, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void nonLandsAreNotAffected() {
        harness.addToBattlefield(player1, new YavimayaCradleOfGrowth());
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new Archangel());

        assertThat(gqs.hasEffectiveSubtype(gd, angel, CardSubtype.FOREST)).isFalse();
    }

    @Test
    void typeAndAbilityAreLostWhenYavimayaLeaves() {
        Permanent yavimaya = harness.addToBattlefieldAndReturn(player1, new YavimayaCradleOfGrowth());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Quicksand());

        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.FOREST)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(yavimaya);

        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.FOREST)).isFalse();
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
