package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AjaniWiseCounselor;
import com.github.laxika.magicalvibes.cards.a.AjaniAdversaryOfTyrants;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CourtCleric.class, AjaniWiseCounselor.class, AjaniAdversaryOfTyrants.class, GreenwoodSentinel.class})
class CourtClericTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 while its controller controls an Ajani planeswalker")
    void getsBoostWithAjaniPlaneswalker() {
        Permanent cleric = addCreatureReady(player1, new CourtCleric());
        int basePower = gqs.getEffectivePower(gd, cleric);
        int baseToughness = gqs.getEffectiveToughness(gd, cleric);

        harness.addToBattlefield(player1, new AjaniWiseCounselor());

        assertThat(gqs.getEffectivePower(gd, cleric)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, cleric)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("Does not get the boost without an Ajani planeswalker")
    void noBoostWithoutAjaniPlaneswalker() {
        addCreatureReady(player1, new CourtCleric());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("An opponent's Ajani planeswalker does not grant the boost")
    void opponentAjaniDoesNotCount() {
        Permanent cleric = addCreatureReady(player1, new CourtCleric());
        int basePower = gqs.getEffectivePower(gd, cleric);
        int baseToughness = gqs.getEffectiveToughness(gd, cleric);

        harness.addToBattlefield(player2, new AjaniWiseCounselor());

        assertThat(gqs.getEffectivePower(gd, cleric)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, cleric)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("An Ajani subtype on a non-planeswalker does not grant the boost")
    void nonPlaneswalkerAjaniDoesNotCount() {
        Permanent cleric = addCreatureReady(player1, new CourtCleric());
        int basePower = gqs.getEffectivePower(gd, cleric);
        int baseToughness = gqs.getEffectiveToughness(gd, cleric);
        Card ajaniCreature = new GreenwoodSentinel();
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
        harness.addToBattlefield(player1, new AjaniWiseCounselor());
        assertThat(gqs.getEffectivePower(gd, cleric)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, cleric)).isEqualTo(baseToughness + 1);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.AJANI));

        assertThat(gqs.getEffectivePower(gd, cleric)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, cleric)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Lifelink gains life equal to the boosted combat damage")
    void lifelinkWithAjaniBoost() {
        addCreatureReady(player1, new CourtCleric());
        harness.enterBattlefieldAndReturn(player1, new AjaniWiseCounselor());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Multiple Ajani planeswalkers grant only one boost, which persists while one remains")
    void multipleAjanisGrantOnlyOneBoost() {
        Permanent cleric = addCreatureReady(player1, new CourtCleric());
        int basePower = gqs.getEffectivePower(gd, cleric);
        int baseToughness = gqs.getEffectiveToughness(gd, cleric);
        Permanent firstAjani = harness.addToBattlefieldAndReturn(player1, new AjaniWiseCounselor());
        harness.addToBattlefield(player1, new AjaniAdversaryOfTyrants());

        assertThat(gqs.getEffectivePower(gd, cleric)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, cleric)).isEqualTo(baseToughness + 1);

        gd.playerBattlefields.get(player1.getId()).remove(firstAjani);

        assertThat(gqs.getEffectivePower(gd, cleric)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, cleric)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("The boost affects only Court Cleric")
    void boostDoesNotAffectOtherCreatures() {
        addCreatureReady(player1, new CourtCleric());
        Permanent sentinel = addCreatureReady(player1, new GreenwoodSentinel());
        int basePower = gqs.getEffectivePower(gd, sentinel);
        int baseToughness = gqs.getEffectiveToughness(gd, sentinel);

        harness.addToBattlefield(player1, new AjaniWiseCounselor());

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(baseToughness);
    }
}
