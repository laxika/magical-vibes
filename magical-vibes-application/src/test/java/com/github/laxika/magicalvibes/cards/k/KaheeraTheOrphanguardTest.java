package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BoneyardLurker;
import com.github.laxika.magicalvibes.cards.f.FrenziedRaptor;
import com.github.laxika.magicalvibes.cards.g.GarrisonCat;
import com.github.laxika.magicalvibes.cards.g.Glimmerbell;
import com.github.laxika.magicalvibes.cards.i.InsatiableHemophage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        KaheeraTheOrphanguard.class,
        GarrisonCat.class,
        Glimmerbell.class,
        InsatiableHemophage.class,
        FrenziedRaptor.class,
        BoneyardLurker.class,
        GrizzlyBears.class
})
class KaheeraTheOrphanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Other qualifying creatures you control get +1/+1 and vigilance")
    void boostsQualifyingCreaturesYouControl() {
        addCreatureReady(player1, new GarrisonCat());
        addCreatureReady(player1, new Glimmerbell());
        addCreatureReady(player1, new InsatiableHemophage());
        addCreatureReady(player1, new FrenziedRaptor());
        addCreatureReady(player1, new BoneyardLurker());

        Permanent cat = findPermanent(player1, "Garrison Cat");
        Permanent elemental = findPermanent(player1, "Glimmerbell");
        Permanent nightmare = findPermanent(player1, "Insatiable Hemophage");
        Permanent dinosaur = findPermanent(player1, "Frenzied Raptor");
        Permanent beast = findPermanent(player1, "Boneyard Lurker");
        int catPower = gqs.getEffectivePower(gd, cat);
        int elementalPower = gqs.getEffectivePower(gd, elemental);
        int nightmarePower = gqs.getEffectivePower(gd, nightmare);
        int dinosaurPower = gqs.getEffectivePower(gd, dinosaur);
        int beastPower = gqs.getEffectivePower(gd, beast);

        harness.addToBattlefield(player1, new KaheeraTheOrphanguard());

        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(catPower + 1);
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(elementalPower + 1);
        assertThat(gqs.getEffectivePower(gd, nightmare)).isEqualTo(nightmarePower + 1);
        assertThat(gqs.getEffectivePower(gd, dinosaur)).isEqualTo(dinosaurPower + 1);
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(beastPower + 1);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nightmare, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, beast, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Nonqualifying creatures and opposing creatures are unaffected")
    void doesNotAffectNonqualifyingOrOpposingCreatures() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GarrisonCat());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent opposingCat = findPermanent(player2, "Garrison Cat");
        int bearsPower = gqs.getEffectivePower(gd, bears);
        int bearsToughness = gqs.getEffectiveToughness(gd, bears);
        int opposingCatPower = gqs.getEffectivePower(gd, opposingCat);
        int opposingCatToughness = gqs.getEffectiveToughness(gd, opposingCat);

        harness.addToBattlefield(player1, new KaheeraTheOrphanguard());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(bearsPower);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(bearsToughness);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opposingCat)).isEqualTo(opposingCatPower);
        assertThat(gqs.getEffectiveToughness(gd, opposingCat)).isEqualTo(opposingCatToughness);
        assertThat(gqs.hasKeyword(gd, opposingCat, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Kaheera does not boost itself")
    void doesNotBoostItself() {
        KaheeraTheOrphanguard card = new KaheeraTheOrphanguard();
        card.setPower(10);
        card.setToughness(10);
        harness.addToBattlefield(player1, card);

        Permanent kaheera = findPermanent(player1, "Kaheera, the Orphanguard");

        assertThat(gqs.getEffectivePower(gd, kaheera)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, kaheera)).isEqualTo(10);
    }
}
