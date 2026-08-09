package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AjaniGoldmane;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CourtClericTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 while its controller controls an Ajani planeswalker")
    void getsBoostWithAjaniPlaneswalker() {
        Permanent cleric = addCreatureReady(player1, new CourtCleric());
        int basePower = gqs.getEffectivePower(gd, cleric);
        int baseToughness = gqs.getEffectiveToughness(gd, cleric);

        harness.addToBattlefield(player1, new AjaniGoldmane());

        assertThat(gqs.getEffectivePower(gd, cleric)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, cleric)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("Does not get the boost without an Ajani planeswalker")
    void noBoostWithoutAjaniPlaneswalker() {
        Permanent cleric = addCreatureReady(player1, new CourtCleric());
        int basePower = gqs.getEffectivePower(gd, cleric);
        int baseToughness = gqs.getEffectiveToughness(gd, cleric);

        assertThat(gqs.getEffectivePower(gd, cleric)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, cleric)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("An opponent's Ajani planeswalker does not grant the boost")
    void opponentAjaniDoesNotCount() {
        Permanent cleric = addCreatureReady(player1, new CourtCleric());
        int basePower = gqs.getEffectivePower(gd, cleric);
        int baseToughness = gqs.getEffectiveToughness(gd, cleric);

        harness.addToBattlefield(player2, new AjaniGoldmane());

        assertThat(gqs.getEffectivePower(gd, cleric)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, cleric)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("An Ajani subtype on a non-planeswalker does not grant the boost")
    void nonPlaneswalkerAjaniDoesNotCount() {
        Permanent cleric = addCreatureReady(player1, new CourtCleric());
        int basePower = gqs.getEffectivePower(gd, cleric);
        int baseToughness = gqs.getEffectiveToughness(gd, cleric);
        Card ajaniCreature = new GrizzlyBears();
        ajaniCreature.setSubtypes(List.of(CardSubtype.AJANI));

        harness.addToBattlefield(player1, ajaniCreature);

        assertThat(gqs.getEffectivePower(gd, cleric)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, cleric)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Loses the boost when the Ajani planeswalker leaves")
    void losesBoostWhenAjaniLeaves() {
        Permanent cleric = addCreatureReady(player1, new CourtCleric());
        int basePower = gqs.getEffectivePower(gd, cleric);
        int baseToughness = gqs.getEffectiveToughness(gd, cleric);
        harness.addToBattlefield(player1, new AjaniGoldmane());
        assertThat(gqs.getEffectivePower(gd, cleric)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, cleric)).isEqualTo(baseToughness + 1);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.AJANI));

        assertThat(gqs.getEffectivePower(gd, cleric)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, cleric)).isEqualTo(baseToughness);
    }
}
