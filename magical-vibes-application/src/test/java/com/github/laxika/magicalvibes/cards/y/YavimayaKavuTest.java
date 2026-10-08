package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.c.CinderShade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.Repulse;
import com.github.laxika.magicalvibes.cards.v.VodalianZombie;
import com.github.laxika.magicalvibes.cards.y.YavimayaBarbarian;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YavimayaKavu.class, CinderShade.class, YavimayaBarbarian.class,
        VodalianZombie.class, Mountain.class, Forest.class, Repulse.class})
class YavimayaKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Power counts red creatures and toughness counts green creatures on the battlefield")
    void countsMatchingCreaturesOnAllBattlefields() {
        Permanent kavu = addCreatureReady(player1, new YavimayaKavu());
        harness.addToBattlefield(player1, new CinderShade());
        harness.addToBattlefield(player2, new CinderShade());
        harness.addToBattlefield(player2, new YavimayaBarbarian());

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not count noncreatures or creatures of other colors")
    void ignoresNonmatchingPermanents() {
        Permanent kavu = addCreatureReady(player1, new YavimayaKavu());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new VodalianZombie());

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power and toughness update as matching creatures enter")
    void updatesWhenCreatureCountsChange() {
        Permanent kavu = addCreatureReady(player1, new YavimayaKavu());

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(1);

        harness.addToBattlefield(player2, new CinderShade());
        harness.addToBattlefield(player1, new YavimayaBarbarian());

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);
    }

    @Test
    @DisplayName("A resolved Kavu counts itself and another Kavu without recursive evaluation")
    void countsMultipleKavuAfterResolving() {
        Permanent first = addCreatureReady(player2, new YavimayaKavu());

        harness.castFromHand(player1, new YavimayaKavu(), "{2}{R}{G}");
        harness.passBothPriorities();

        Permanent second = findPermanent(player1, "Yavimaya Kavu");
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power decreases immediately when a red creature leaves the battlefield")
    void updatesWhenMatchingCreatureLeaves() {
        Permanent kavu = addCreatureReady(player1, new YavimayaKavu());
        Permanent shade = harness.addToBattlefieldAndReturn(player2, new CinderShade());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Repulse()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(2);
        harness.castAndResolveInstant(player1, 0, shade.getId());

        harness.assertInHand(player2, "Cinder Shade");
        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(1);
    }

    @Test
    @DisplayName("The defining abilities work in the graveyard and do not count cards outside the battlefield")
    void definesPowerAndToughnessOutsideBattlefield() {
        YavimayaKavu kavu = new YavimayaKavu();
        harness.setGraveyard(player1, List.of(kavu, new YavimayaBarbarian()));
        harness.setHand(player2, List.of(new CinderShade()));

        assertThat(gqs.getEffectiveCardPower(gd, kavu)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, kavu)).isZero();

        harness.addToBattlefield(player1, new CinderShade());
        harness.addToBattlefield(player2, new YavimayaBarbarian());

        assertThat(gqs.getEffectiveCardPower(gd, kavu)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, kavu)).isEqualTo(1);
    }
}
