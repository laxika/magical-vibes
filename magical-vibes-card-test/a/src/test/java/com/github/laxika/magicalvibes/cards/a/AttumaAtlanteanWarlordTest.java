package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AttumaAtlanteanWarlord.class, CoralMerfolk.class, GrizzlyBears.class})
class AttumaAtlanteanWarlordTest extends BaseCardTest {

    @Test
    @DisplayName("Other Merfolk get +1/+1, but Attuma and non-Merfolk creatures do not")
    void boostsOtherMerfolkOnly() {
        Permanent merfolk = addCreatureReady(player1, new CoralMerfolk());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        int merfolkPower = gqs.getEffectivePower(gd, merfolk);
        int merfolkToughness = gqs.getEffectiveToughness(gd, merfolk);
        int bearsPower = gqs.getEffectivePower(gd, bears);
        int bearsToughness = gqs.getEffectiveToughness(gd, bears);

        Permanent attuma = addCreatureReady(player1, new AttumaAtlanteanWarlord());

        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(merfolkPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(merfolkToughness + 1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(bearsPower);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(bearsToughness);
        assertThat(gqs.getEffectivePower(gd, attuma)).isEqualTo(attuma.getCard().getPower());
        assertThat(gqs.getEffectiveToughness(gd, attuma)).isEqualTo(attuma.getCard().getToughness());
    }

    @Test
    @DisplayName("Draws once when one or more Merfolk attack the same player")
    void drawsOnceForMultipleMerfolkAttackers() {
        addCreatureReady(player1, new AttumaAtlanteanWarlord());
        Permanent firstMerfolk = addCreatureReady(player1, new CoralMerfolk());
        Permanent secondMerfolk = addCreatureReady(player1, new CoralMerfolk());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstMerfolk),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondMerfolk)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when only non-Merfolk creatures attack")
    void doesNotDrawForNonMerfolkAttackers() {
        addCreatureReady(player1, new AttumaAtlanteanWarlord());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bears)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Attuma draws when it attacks alone")
    void drawsWhenAttumaAttacksAlone() {
        addCreatureReady(player1, new AttumaAtlanteanWarlord());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opposing Merfolk do not get the boost")
    void doesNotBoostOpposingMerfolk() {
        Permanent merfolk = addCreatureReady(player2, new CoralMerfolk());
        int power = gqs.getEffectivePower(gd, merfolk);
        int toughness = gqs.getEffectiveToughness(gd, merfolk);

        addCreatureReady(player1, new AttumaAtlanteanWarlord());

        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(toughness);
    }

    @Test
    @DisplayName("Opposing Merfolk attacking do not trigger Attuma")
    void doesNotDrawForOpposingMerfolk() {
        addCreatureReady(player1, new AttumaAtlanteanWarlord());
        addCreatureReady(player2, new CoralMerfolk());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(player2, List.of(0));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The boost ends when Attuma leaves the battlefield")
    void boostEndsWhenAttumaLeaves() {
        Permanent merfolk = addCreatureReady(player1, new CoralMerfolk());
        int power = gqs.getEffectivePower(gd, merfolk);
        int toughness = gqs.getEffectiveToughness(gd, merfolk);
        Permanent attuma = addCreatureReady(player1, new AttumaAtlanteanWarlord());
        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(power + 1);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(toughness + 1);

        gd.playerBattlefields.get(player1.getId()).remove(attuma);

        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(toughness);
    }

    @Test
    @DisplayName("An attack trigger still draws after Attuma and the attacking Merfolk leave")
    void drawTriggerSurvivesSourceAndAttackerLeaving() {
        Permanent attuma = addCreatureReady(player1, new AttumaAtlanteanWarlord());
        Permanent merfolk = addCreatureReady(player1, new CoralMerfolk());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(merfolk);
        gd.playerBattlefields.get(player1.getId()).remove(attuma);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
