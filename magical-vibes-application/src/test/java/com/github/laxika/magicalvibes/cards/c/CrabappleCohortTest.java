package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AshenmoorCohort;
import com.github.laxika.magicalvibes.cards.s.ShieldOfTheOversoul;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrabappleCohort.class, AshenmoorCohort.class, ShieldOfTheOversoul.class})
class CrabappleCohortTest extends BaseCardTest {

    @Test
    @DisplayName("Base 4/4 when no other green creature is controlled")
    void noBoostWhenAlone() {
        Permanent cohort = addCreatureReady(player1, new CrabappleCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +1/+1 when controller controls another green creature")
    void boostWithAnotherGreenCreature() {
        Permanent cohort = addCreatureReady(player1, new CrabappleCohort());
        addCreatureReady(player1, new CrabappleCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost is only +1/+1 even with multiple green creatures")
    void boostDoesNotStack() {
        Permanent cohort = addCreatureReady(player1, new CrabappleCohort());
        addCreatureReady(player1, new CrabappleCohort());
        addCreatureReady(player1, new CrabappleCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(5);
    }

    @Test
    @DisplayName("Two Crabapple Cohorts alone boost each other (each is another green creature)")
    void twoCohortsBoostEachOther() {
        Permanent firstCohort = addCreatureReady(player1, new CrabappleCohort());
        Permanent secondCohort = addCreatureReady(player1, new CrabappleCohort());

        assertThat(gqs.getEffectivePower(gd, firstCohort)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, firstCohort)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, secondCohort)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, secondCohort)).isEqualTo(5);
    }

    @Test
    @DisplayName("A non-green creature does not grant the boost")
    void noBoostWithNonGreenCreature() {
        Permanent cohort = addCreatureReady(player1, new CrabappleCohort());
        addCreatureReady(player1, new AshenmoorCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(4);
    }

    @Test
    @DisplayName("A green noncreature permanent does not grant the boost")
    void noBoostWithGreenNonCreaturePermanent() {
        Permanent cohort = addCreatureReady(player1, new CrabappleCohort());
        harness.addToBattlefield(player1, new ShieldOfTheOversoul());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's green creature does not grant the boost")
    void opponentGreenCreatureDoesNotCount() {
        Permanent cohort = addCreatureReady(player1, new CrabappleCohort());
        addCreatureReady(player2, new CrabappleCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(4);
    }

    @Test
    @DisplayName("Loses the boost when the other green creature leaves the battlefield")
    void losesBoostWhenGreenCreatureLeaves() {
        Permanent cohort = addCreatureReady(player1, new CrabappleCohort());
        Permanent otherCohort = addCreatureReady(player1, new CrabappleCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(otherCohort);

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(4);
    }

    @Test
    @DisplayName("Static boost survives end-of-turn modifier reset")
    void staticBoostSurvivesEndOfTurnReset() {
        Permanent cohort = addCreatureReady(player1, new CrabappleCohort());
        addCreatureReady(player1, new CrabappleCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(5);

        cohort.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(5);
    }
}
