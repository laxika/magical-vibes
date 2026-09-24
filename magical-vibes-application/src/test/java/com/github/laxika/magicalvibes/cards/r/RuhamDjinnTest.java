package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinSpy;
import com.github.laxika.magicalvibes.cards.k.KavuLair;
import com.github.laxika.magicalvibes.cards.m.MetathranZombie;
import com.github.laxika.magicalvibes.cards.y.YavimayaBarbarian;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuhamDjinn.class, Forest.class, GoblinSpy.class, KavuLair.class,
        MetathranZombie.class, YavimayaBarbarian.class})
class RuhamDjinnTest extends BaseCardTest {

    private Permanent addRuhamDjinn() {
        return harness.addToBattlefieldAndReturn(player1, new RuhamDjinn());
    }

    @Test
    @DisplayName("Shrinks when white is the most common color")
    void shrinksWhenWhiteIsMostCommon() {
        Permanent ruham = addRuhamDjinn();

        assertThat(gqs.getEffectivePower(gd, ruham)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ruham)).isEqualTo(3);
    }

    @Test
    @DisplayName("Shrinks when white is tied for most common color")
    void shrinksWhenWhiteIsTied() {
        Permanent ruham = addRuhamDjinn();
        harness.addToBattlefield(player2, new MetathranZombie());

        assertThat(gqs.getEffectivePower(gd, ruham)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ruham)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not shrink when another color is more common")
    void doesNotShrinkWhenAnotherColorIsMoreCommon() {
        Permanent ruham = addRuhamDjinn();
        harness.addToBattlefield(player2, new MetathranZombie());
        harness.addToBattlefield(player2, new MetathranZombie());

        assertThat(gqs.getEffectivePower(gd, ruham)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ruham)).isEqualTo(5);
    }

    @Test
    @DisplayName("Counts each color of multicolored permanents")
    void countsEachColorOfMulticoloredPermanents() {
        Permanent ruham = addRuhamDjinn();
        harness.addToBattlefield(player2,
                new YavimayaBarbarian());
        harness.addToBattlefield(player2,
                new GoblinSpy());

        assertThat(gqs.getEffectivePower(gd, ruham)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ruham)).isEqualTo(5);
    }

    @Test
    @DisplayName("Counts colored noncreature permanents")
    void countsColoredNoncreaturePermanents() {
        Permanent ruham = addRuhamDjinn();
        harness.addToBattlefield(player2, new KavuLair());

        assertThat(gqs.getEffectivePower(gd, ruham)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ruham)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not count colorless permanents")
    void doesNotCountColorlessPermanents() {
        Permanent ruham = addRuhamDjinn();
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, ruham)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ruham)).isEqualTo(3);
    }
}
