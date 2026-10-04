package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.Archangel;
import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.q.Quicksand;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlanketOfNight.class, Quicksand.class, Archangel.class, Forest.class, BloodMoon.class})
class BlanketOfNightTest extends BaseCardTest {

    @Test
    void allLandsGainSwampSubtype() {
        harness.addToBattlefield(player1, new BlanketOfNight());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Quicksand());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Quicksand());

        assertThat(gqs.hasEffectiveSubtype(gd, ownLand, CardSubtype.SWAMP)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, opponentLand, CardSubtype.SWAMP)).isTrue();
    }

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
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void resolvingBlanketAffectsLandsAlreadyOnBattlefield() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Quicksand());
        harness.castFromHand(player1, new BlanketOfNight(), "{1}{B}{B}");
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.SWAMP)).isTrue();
        harness.activateAbility(player1, 0, 2, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void printedManaAbilityDoesNotAlsoProduceBlack() {
        harness.addToBattlefield(player1, new BlanketOfNight());
        harness.addToBattlefield(player1, new Quicksand());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void forestRetainsGreenManaAbility() {
        harness.addToBattlefield(player1, new BlanketOfNight());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void multipleBlanketsDoNotIncreaseManaProduced() {
        harness.addToBattlefield(player1, new BlanketOfNight());
        harness.addToBattlefield(player1, new BlanketOfNight());
        harness.addToBattlefield(player1, new Quicksand());

        harness.activateAbility(player1, 2, 2, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 2, 3, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void laterBloodMoonRemovesSwampManaAbility() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Quicksand());
        harness.enterBattlefieldAndReturn(player1, new BlanketOfNight());
        harness.enterBattlefieldAndReturn(player1, new BloodMoon());

        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.SWAMP)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.MOUNTAIN)).isTrue();
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void laterBlanketAddsSwampManaAbilityToBloodMoonLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Quicksand());
        harness.enterBattlefieldAndReturn(player1, new BloodMoon());
        harness.enterBattlefieldAndReturn(player1, new BlanketOfNight());

        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.MOUNTAIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.SWAMP)).isTrue();
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void blackManaAbilityDisappearsWhenBlanketLeaves() {
        Permanent blanket = harness.addToBattlefieldAndReturn(player1, new BlanketOfNight());
        harness.addToBattlefield(player1, new Quicksand());
        gd.playerBattlefields.get(player1.getId()).remove(blanket);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }
}
