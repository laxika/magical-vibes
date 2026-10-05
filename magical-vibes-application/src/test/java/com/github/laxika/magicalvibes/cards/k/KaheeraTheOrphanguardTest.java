package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BoneyardLurker;
import com.github.laxika.magicalvibes.cards.c.CheckpointOfficer;
import com.github.laxika.magicalvibes.cards.f.FrenziedRaptor;
import com.github.laxika.magicalvibes.cards.g.GarrisonCat;
import com.github.laxika.magicalvibes.cards.g.Glimmerbell;
import com.github.laxika.magicalvibes.cards.i.InsatiableHemophage;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        KaheeraTheOrphanguard.class,
        GarrisonCat.class,
        Glimmerbell.class,
        InsatiableHemophage.class,
        FrenziedRaptor.class,
        BoneyardLurker.class,
        CheckpointOfficer.class
})
class KaheeraTheOrphanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Other qualifying creatures you control get +1/+1 and vigilance")
    void boostsQualifyingCreaturesYouControl() {
        Permanent cat = addCreatureReady(player1, new GarrisonCat());
        Permanent elemental = addCreatureReady(player1, new Glimmerbell());
        Permanent nightmare = addCreatureReady(player1, new InsatiableHemophage());
        Permanent dinosaur = addCreatureReady(player1, new FrenziedRaptor());
        Permanent beast = addCreatureReady(player1, new BoneyardLurker());

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
        Permanent officer = addCreatureReady(player1, new CheckpointOfficer());
        Permanent opposingCat = addCreatureReady(player2, new GarrisonCat());

        int officerPower = gqs.getEffectivePower(gd, officer);
        int officerToughness = gqs.getEffectiveToughness(gd, officer);
        int opposingCatPower = gqs.getEffectivePower(gd, opposingCat);
        int opposingCatToughness = gqs.getEffectiveToughness(gd, opposingCat);

        harness.addToBattlefield(player1, new KaheeraTheOrphanguard());

        assertThat(gqs.getEffectivePower(gd, officer)).isEqualTo(officerPower);
        assertThat(gqs.getEffectiveToughness(gd, officer)).isEqualTo(officerToughness);
        assertThat(gqs.hasKeyword(gd, officer, Keyword.VIGILANCE)).isFalse();
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
        Permanent kaheera = harness.addToBattlefieldAndReturn(player1, card);

        assertThat(gqs.getEffectivePower(gd, kaheera)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, kaheera)).isEqualTo(10);
    }

    @Test
    @DisplayName("A creature with two qualifying types gets only one +1/+1 bonus")
    void multipleQualifyingTypesDoNotMultiplyBonus() {
        Permanent lurker = harness.addToBattlefieldAndReturn(player1, new BoneyardLurker());
        int power = gqs.getEffectivePower(gd, lurker);
        int toughness = gqs.getEffectiveToughness(gd, lurker);

        harness.addToBattlefield(player1, new KaheeraTheOrphanguard());

        assertThat(gqs.getEffectivePower(gd, lurker)).isEqualTo(power + 1);
        assertThat(gqs.getEffectiveToughness(gd, lurker)).isEqualTo(toughness + 1);
        assertThat(gqs.hasKeyword(gd, lurker, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Qualifying creatures entering later gain the bonus and lose it when Kaheera leaves")
    void bonusTracksKaheerasPresence() {
        Permanent kaheera = harness.addToBattlefieldAndReturn(player1, new KaheeraTheOrphanguard());
        Permanent cat = harness.addToBattlefieldAndReturn(player1, new GarrisonCat());
        int boostedPower = gqs.getEffectivePower(gd, cat);
        int boostedToughness = gqs.getEffectiveToughness(gd, cat);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.VIGILANCE)).isTrue();

        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, kaheera);

        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(boostedPower - 1);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(boostedToughness - 1);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Granted vigilance lets a qualifying creature attack without tapping")
    void qualifyingAttackerDoesNotTap() {
        Permanent cat = addCreatureReady(player1, new GarrisonCat());
        harness.addToBattlefield(player1, new KaheeraTheOrphanguard());

        declareAttackers(List.of(0));

        assertThat(cat.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Every qualifying creature type receives the toughness bonus")
    void boostsToughnessOfEveryQualifyingType() {
        List<Permanent> creatures = List.of(
                harness.addToBattlefieldAndReturn(player1, new GarrisonCat()),
                harness.addToBattlefieldAndReturn(player1, new Glimmerbell()),
                harness.addToBattlefieldAndReturn(player1, new InsatiableHemophage()),
                harness.addToBattlefieldAndReturn(player1, new FrenziedRaptor()),
                harness.addToBattlefieldAndReturn(player1, new BoneyardLurker()));
        List<Integer> toughnesses = creatures.stream()
                .map(creature -> gqs.getEffectiveToughness(gd, creature)).toList();

        harness.addToBattlefield(player1, new KaheeraTheOrphanguard());

        for (int i = 0; i < creatures.size(); i++) {
            assertThat(gqs.getEffectiveToughness(gd, creatures.get(i))).isEqualTo(toughnesses.get(i) + 1);
        }
    }
}
