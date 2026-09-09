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
        assertThat(gqs.getEffectivePower(gd, attuma)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attuma)).isEqualTo(4);
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
}
