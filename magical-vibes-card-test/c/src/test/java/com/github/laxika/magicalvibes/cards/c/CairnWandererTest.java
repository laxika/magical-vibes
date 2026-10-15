package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AxegrinderGiant;
import com.github.laxika.magicalvibes.cards.b.BogStriderAsh;
import com.github.laxika.magicalvibes.cards.b.BurrentonForgeTender;
import com.github.laxika.magicalvibes.cards.b.BrionStoutarm;
import com.github.laxika.magicalvibes.cards.d.Dread;
import com.github.laxika.magicalvibes.cards.m.MoongloveWinnower;
import com.github.laxika.magicalvibes.cards.o.OakgnarlWarrior;
import com.github.laxika.magicalvibes.cards.p.PloverKnights;
import com.github.laxika.magicalvibes.cards.s.SkyhunterSkirmisher;
import com.github.laxika.magicalvibes.cards.s.SphinxOfJwarIsle;
import com.github.laxika.magicalvibes.cards.w.WingsOfVelisVel;
import com.github.laxika.magicalvibes.cards.z.ZodiacRooster;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        CairnWanderer.class,
        PloverKnights.class,
        Cloudthresher.class,
        BogStriderAsh.class,
        OakgnarlWarrior.class,
        AxegrinderGiant.class,
        BurrentonForgeTender.class,
        ZodiacRooster.class,
        BrionStoutarm.class,
        Dread.class,
        MoongloveWinnower.class,
        ChangelingBerserker.class,
        SkyhunterSkirmisher.class,
        SphinxOfJwarIsle.class,
        WingsOfVelisVel.class
})
class CairnWandererTest extends BaseCardTest {

    @Test
    @DisplayName("Gains a watched keyword from a creature card in the controller's graveyard")
    void gainsKeywordFromOwnGraveyard() {
        Permanent wanderer = addCreatureReady(player1, new CairnWanderer());
        // Plover Knights has flying and first strike.
        harness.setGraveyard(player1, List.of(new PloverKnights()));

        var keywords = gqs.computeStaticBonus(gd, wanderer).keywords();

        assertThat(keywords).contains(Keyword.FLYING, Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Gains a watched keyword from a creature card in an opponent's graveyard")
    void gainsKeywordFromOpponentGraveyard() {
        Permanent wanderer = addCreatureReady(player1, new CairnWanderer());
        harness.setGraveyard(player2, List.of(new Cloudthresher()));

        assertThat(gqs.computeStaticBonus(gd, wanderer).keywords()).contains(Keyword.REACH);
    }

    @Test
    @DisplayName("Gains the exact landwalk variant present in a graveyard")
    void gainsLandwalkVariant() {
        Permanent wanderer = addCreatureReady(player1, new CairnWanderer());
        // Bog-Strider Ash has swampwalk.
        harness.setGraveyard(player1, List.of(new BogStriderAsh()));

        var keywords = gqs.computeStaticBonus(gd, wanderer).keywords();

        assertThat(keywords).contains(Keyword.SWAMPWALK);
        assertThat(keywords).doesNotContain(
                Keyword.FORESTWALK,
                Keyword.ISLANDWALK,
                Keyword.MOUNTAINWALK,
                Keyword.PLAINSWALK
        );
    }

    @Test
    @DisplayName("Combines watched keywords from creature cards across all graveyards")
    void combinesKeywordsFromAllGraveyards() {
        Permanent wanderer = addCreatureReady(player1, new CairnWanderer());
        harness.setGraveyard(player1, List.of(new PloverKnights()));
        harness.setGraveyard(player2, List.of(new OakgnarlWarrior()));

        var keywords = gqs.computeStaticBonus(gd, wanderer).keywords();

        assertThat(keywords).contains(Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.VIGILANCE, Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Does not gain keywords outside the watched list")
    void doesNotGainUnwatchedKeyword() {
        Permanent wanderer = addCreatureReady(player1, new CairnWanderer());
        // Cloudthresher has flash, which Cairn Wanderer does not grant.
        harness.setGraveyard(player1, List.of(new Cloudthresher()));

        assertThat(gqs.computeStaticBonus(gd, wanderer).keywords()).doesNotContain(Keyword.FLASH);
    }

    @Test
    @DisplayName("Gains nothing from vanilla creature cards in a graveyard")
    void gainsNothingFromVanillaCreature() {
        Permanent wanderer = addCreatureReady(player1, new CairnWanderer());
        harness.setGraveyard(player1, List.of(new AxegrinderGiant()));

        assertThat(gqs.computeStaticBonus(gd, wanderer).keywords())
                .doesNotContain(Keyword.FLYING, Keyword.TRAMPLE, Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("Gains plainswalk from a creature card in a graveyard")
    void gainsPlainswalk() {
        Permanent wanderer = addCreatureReady(player1, new CairnWanderer());
        harness.setGraveyard(player1, List.of(new ZodiacRooster()));

        assertThat(gqs.computeStaticBonus(gd, wanderer).keywords()).contains(Keyword.PLAINSWALK);
    }

    @Test
    @DisplayName("Gains protection from a creature card in a graveyard")
    void gainsProtectionFromRed() {
        Permanent wanderer = addCreatureReady(player1, new CairnWanderer());
        harness.setGraveyard(player1, List.of(new BurrentonForgeTender()));

        assertThat(gqs.hasProtectionFrom(gd, wanderer, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("Loses the keyword when the creature card leaves the graveyard")
    void losesKeywordWhenCardLeavesGraveyard() {
        Permanent wanderer = addCreatureReady(player1, new CairnWanderer());
        harness.setGraveyard(player1, List.of(new PloverKnights()));
        assertThat(gqs.computeStaticBonus(gd, wanderer).keywords()).contains(Keyword.FLYING);

        harness.setGraveyard(player1, List.of());

        assertThat(gqs.computeStaticBonus(gd, wanderer).keywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Gains fear, deathtouch, haste, and lifelink from creature cards in graveyards")
    void gainsRemainingLrwKeywords() {
        Permanent wanderer = addCreatureReady(player1, new CairnWanderer());
        harness.setGraveyard(player1, List.of(new Dread(), new MoongloveWinnower()));
        harness.setGraveyard(player2, List.of(new ChangelingBerserker(), new BrionStoutarm()));

        assertThat(gqs.computeStaticBonus(gd, wanderer).keywords())
                .contains(Keyword.FEAR, Keyword.DEATHTOUCH, Keyword.HASTE, Keyword.LIFELINK)
                .doesNotContain(Keyword.FLASH);
    }

    @Test
    @DisplayName("Gains double strike and shroud from creature cards in graveyards")
    void gainsDoubleStrikeAndShroud() {
        Permanent wanderer = addCreatureReady(player1, new CairnWanderer());
        harness.setGraveyard(player1, List.of(new SkyhunterSkirmisher()));
        harness.setGraveyard(player2, List.of(new SphinxOfJwarIsle()));

        assertThat(gqs.computeStaticBonus(gd, wanderer).keywords())
                .contains(Keyword.DOUBLE_STRIKE, Keyword.SHROUD, Keyword.FLYING);
    }

    @Test
    @DisplayName("Keeps a keyword while another graveyard still contains a creature card with it")
    void keepsKeywordUntilLastProviderLeaves() {
        Permanent wanderer = addCreatureReady(player1, new CairnWanderer());
        harness.setGraveyard(player1, List.of(new PloverKnights()));
        harness.setGraveyard(player2, List.of(new PloverKnights()));
        assertThat(gqs.computeStaticBonus(gd, wanderer).keywords()).contains(Keyword.FLYING);

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.computeStaticBonus(gd, wanderer).keywords()).contains(Keyword.FLYING);

        harness.setGraveyard(player2, List.of());
        assertThat(gqs.computeStaticBonus(gd, wanderer).keywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Does not gain flying from a kindred instant that grants flying")
    void ignoresNoncreatureCardsInGraveyards() {
        Permanent wanderer = addCreatureReady(player1, new CairnWanderer());
        harness.setGraveyard(player1, List.of(new WingsOfVelisVel()));

        assertThat(gqs.computeStaticBonus(gd, wanderer).keywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Does not share its gained keywords with other creatures")
    void grantsKeywordsOnlyToItself() {
        Permanent wanderer = addCreatureReady(player1, new CairnWanderer());
        Permanent giant = addCreatureReady(player1, new AxegrinderGiant());
        harness.setGraveyard(player2, List.of(new PloverKnights()));

        assertThat(gqs.computeStaticBonus(gd, wanderer).keywords()).contains(Keyword.FLYING);
        assertThat(gqs.computeStaticBonus(gd, giant).keywords())
                .doesNotContain(Keyword.FLYING, Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Does not gain keywords from creature cards outside graveyards")
    void ignoresOtherZones() {
        Permanent wanderer = addCreatureReady(player1, new CairnWanderer());
        addCreatureReady(player2, new PloverKnights());
        harness.setHand(player1, List.of(new PloverKnights()));
        harness.setLibrary(player2, List.of(new PloverKnights()));
        harness.setExile(player1, List.of(new PloverKnights()));

        assertThat(gqs.computeStaticBonus(gd, wanderer).keywords())
                .doesNotContain(Keyword.FLYING, Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Gains only the protection present in an opponent's graveyard and loses it when removed")
    void tracksProtectionInOpponentGraveyard() {
        Permanent wanderer = addCreatureReady(player1, new CairnWanderer());
        harness.setGraveyard(player2, List.of(new BurrentonForgeTender()));

        assertThat(gqs.hasProtectionFrom(gd, wanderer, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, wanderer, CardColor.BLUE)).isFalse();

        harness.setGraveyard(player2, List.of());
        assertThat(gqs.hasProtectionFrom(gd, wanderer, CardColor.RED)).isFalse();
    }
}
