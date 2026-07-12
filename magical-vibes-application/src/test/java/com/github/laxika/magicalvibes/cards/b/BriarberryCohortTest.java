package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BriarberryCohortTest extends BaseCardTest {

    @Test
    @DisplayName("Base 1/1 when no other blue creature is controlled")
    void noBoostWhenAlone() {
        harness.addToBattlefield(player1, new BriarberryCohort());

        Permanent cohort = findPermanent(player1, "Briarberry Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(1);
    }

    @Test
    @DisplayName("No boost with a non-blue creature")
    void noBoostWithNonBlueCreature() {
        harness.addToBattlefield(player1, new BriarberryCohort());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent cohort = findPermanent(player1, "Briarberry Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(1);
    }

    @Test
    @DisplayName("No boost with a blue non-creature permanent (Island is blue but not a creature)")
    void noBoostWithBlueNonCreature() {
        harness.addToBattlefield(player1, new BriarberryCohort());
        harness.addToBattlefield(player1, new Island());

        Permanent cohort = findPermanent(player1, "Briarberry Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +1/+1 when controller controls another blue creature")
    void boostWithAnotherBlueCreature() {
        harness.addToBattlefield(player1, new BriarberryCohort());
        harness.addToBattlefield(player1, new FugitiveWizard());

        Permanent cohort = findPermanent(player1, "Briarberry Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost does not stack with multiple blue creatures")
    void boostDoesNotStack() {
        harness.addToBattlefield(player1, new BriarberryCohort());
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player1, new FugitiveWizard());

        Permanent cohort = findPermanent(player1, "Briarberry Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Briarberry Cohorts boost each other (each is another blue creature)")
    void twoCohortsBoostEachOther() {
        harness.addToBattlefield(player1, new BriarberryCohort());
        harness.addToBattlefield(player1, new BriarberryCohort());

        for (Permanent cohort : gd.playerBattlefields.get(player1.getId())) {
            assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Opponent's blue creature does not grant the boost")
    void opponentBlueCreatureDoesNotCount() {
        harness.addToBattlefield(player1, new BriarberryCohort());
        harness.addToBattlefield(player2, new FugitiveWizard());

        Permanent cohort = findPermanent(player1, "Briarberry Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(1);
    }

    @Test
    @DisplayName("Loses boost when the other blue creature leaves the battlefield")
    void losesBoostWhenBlueCreatureLeaves() {
        harness.addToBattlefield(player1, new BriarberryCohort());
        harness.addToBattlefield(player1, new FugitiveWizard());

        Permanent cohort = findPermanent(player1, "Briarberry Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Fugitive Wizard"));

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(1);
    }

}
