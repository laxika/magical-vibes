package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BlanketOfNight.class, Quicksand.class, Archangel.class})
class BlanketOfNightTest extends BaseCardTest {

    @Test
    void allLandsGainSwampSubtype() {
        harness.addToBattlefield(player1, new BlanketOfNight());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Quicksand());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Quicksand());

        assertThat(gqs.hasEffectiveSubtype(gd, ownLand, CardSubtype.SWAMP)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, opponentLand, CardSubtype.SWAMP)).isTrue();
    }

    @CardUsed(Forest.class)
    @Test
    void landRetainsItsOtherLandTypes() {
        harness.addToBattlefield(player1, new BlanketOfNight());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.hasEffectiveSubtype(gd, forest, CardSubtype.FOREST)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, forest, CardSubtype.SWAMP)).isTrue();
    }

    @Test
    void landCanTapForBlack() {
        harness.addToBattlefield(player1, new BlanketOfNight());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Quicksand());
        // Quicksand has two printed activated abilities; the granted Swamp ability is index 2.
        harness.activateAbility(player1, 1, 2, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void opponentLandCanTapForBlack() {
        harness.addToBattlefield(player1, new BlanketOfNight());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Quicksand());
        harness.activateAbility(player2, 0, 2, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void nonLandsAreNotAffected() {
        harness.addToBattlefield(player1, new BlanketOfNight());
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new Archangel());

        assertThat(gqs.hasEffectiveSubtype(gd, angel, CardSubtype.SWAMP)).isFalse();
    }

    @Test
    void typeAndAbilityLostWhenBlanketLeaves() {
        Permanent blanket = harness.addToBattlefieldAndReturn(player1, new BlanketOfNight());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Quicksand());

        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.SWAMP)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(blanket);

        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.SWAMP)).isFalse();
        // Only Quicksand's printed colorless mana ability remains.
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }
}
