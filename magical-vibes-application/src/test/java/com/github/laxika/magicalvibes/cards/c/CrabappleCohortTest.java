package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CrabappleCohortTest extends BaseCardTest {

    @Test
    @DisplayName("Base 4/4 when no other green creature is controlled")
    void noBoostWhenAlone() {
        harness.addToBattlefield(player1, new CrabappleCohort());

        Permanent cohort = findPermanent(player1, "Crabapple Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(4);
    }

    @Test
    @DisplayName("No boost with a non-green creature")
    void noBoostWithNonGreenCreature() {
        harness.addToBattlefield(player1, new CrabappleCohort());
        harness.addToBattlefield(player1, new HillGiant());

        Permanent cohort = findPermanent(player1, "Crabapple Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +1/+1 when controller controls another green creature")
    void boostWithAnotherGreenCreature() {
        harness.addToBattlefield(player1, new CrabappleCohort());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent cohort = findPermanent(player1, "Crabapple Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost is only +1/+1 even with multiple green creatures")
    void boostDoesNotStack() {
        harness.addToBattlefield(player1, new CrabappleCohort());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent cohort = findPermanent(player1, "Crabapple Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(5);
    }

    @Test
    @DisplayName("Two Crabapple Cohorts alone boost each other (each is another green creature)")
    void twoCohortsBoostEachOther() {
        harness.addToBattlefield(player1, new CrabappleCohort());
        harness.addToBattlefield(player1, new CrabappleCohort());

        List<Permanent> cohorts = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Crabapple Cohort"))
                .toList();

        assertThat(cohorts).hasSize(2);
        for (Permanent cohort : cohorts) {
            assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(5);
        }
    }

    @Test
    @DisplayName("Opponent's green creature does not grant the boost")
    void opponentGreenCreatureDoesNotCount() {
        harness.addToBattlefield(player1, new CrabappleCohort());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent cohort = findPermanent(player1, "Crabapple Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(4);
    }

    @Test
    @DisplayName("Loses boost when the other green creature leaves the battlefield")
    void losesBoostWhenGreenCreatureLeaves() {
        harness.addToBattlefield(player1, new CrabappleCohort());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent cohort = findPermanent(player1, "Crabapple Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(4);
    }

    @Test
    @DisplayName("Static boost survives end-of-turn modifier reset")
    void staticBoostSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new CrabappleCohort());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent cohort = findPermanent(player1, "Crabapple Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(5);

        cohort.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(5);
    }
}
