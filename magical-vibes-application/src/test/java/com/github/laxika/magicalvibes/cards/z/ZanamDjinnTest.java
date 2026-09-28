package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.a.AlloyGolem;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinSpy;
import com.github.laxika.magicalvibes.cards.k.KavuLair;
import com.github.laxika.magicalvibes.cards.u.UrborgSkeleton;
import com.github.laxika.magicalvibes.cards.v.VodalianZombie;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZanamDjinn.class, AlloyGolem.class, Forest.class, GoblinSpy.class,
        KavuLair.class, UrborgSkeleton.class, VodalianZombie.class})
class ZanamDjinnTest extends BaseCardTest {

    private Permanent addZanamDjinn() {
        return harness.addToBattlefieldAndReturn(player1, new ZanamDjinn());
    }

    @Test
    @DisplayName("Shrinks when blue is the most common color")
    void shrinksWhenBlueIsMostCommon() {
        Permanent zanam = addZanamDjinn();

        assertThat(gqs.getEffectivePower(gd, zanam)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zanam)).isEqualTo(4);
    }

    @Test
    @DisplayName("Shrinks when blue is tied for most common color")
    void shrinksWhenBlueIsTied() {
        Permanent zanam = addZanamDjinn();
        harness.addToBattlefield(player2, new GoblinSpy());

        assertThat(gqs.getEffectivePower(gd, zanam)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zanam)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not shrink when another color is more common")
    void doesNotShrinkWhenAnotherColorIsMoreCommon() {
        Permanent zanam = addZanamDjinn();
        harness.addToBattlefield(player2, new GoblinSpy());
        harness.addToBattlefield(player2, new GoblinSpy());

        assertThat(gqs.getEffectivePower(gd, zanam)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, zanam)).isEqualTo(6);
    }

    @Test
    @DisplayName("Counts each color of multicolored permanents")
    void countsEachColorOfMulticoloredPermanents() {
        Permanent zanam = addZanamDjinn();
        harness.addToBattlefield(player2, new VodalianZombie());
        harness.addToBattlefield(player2, new UrborgSkeleton());

        assertThat(gqs.getEffectivePower(gd, zanam)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zanam)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts colored noncreature permanents")
    void countsColoredNoncreaturePermanents() {
        Permanent zanam = addZanamDjinn();
        harness.addToBattlefield(player2, new KavuLair());
        harness.addToBattlefield(player2, new KavuLair());

        assertThat(gqs.getEffectivePower(gd, zanam)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, zanam)).isEqualTo(6);
    }

    @Test
    @DisplayName("Does not count colorless permanents")
    void doesNotCountColorlessPermanents() {
        Permanent zanam = addZanamDjinn();
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, zanam)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zanam)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts colors granted by static effects")
    void countsColorsGrantedByStaticEffects() {
        Permanent zanam = addZanamDjinn();
        Permanent firstGolem = harness.addToBattlefieldAndReturn(player2, new AlloyGolem());
        Permanent secondGolem = harness.addToBattlefieldAndReturn(player2, new AlloyGolem());
        firstGolem.setChosenColor(CardColor.RED);
        secondGolem.setChosenColor(CardColor.RED);

        assertThat(gqs.getEffectivePower(gd, zanam)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, zanam)).isEqualTo(6);
    }
}
