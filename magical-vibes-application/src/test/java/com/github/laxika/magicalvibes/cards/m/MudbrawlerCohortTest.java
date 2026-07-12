package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MudbrawlerCohortTest extends BaseCardTest {

    @Test
    @DisplayName("Base 1/1 when no other red creature is controlled")
    void noBoostWhenAlone() {
        harness.addToBattlefield(player1, new MudbrawlerCohort());

        Permanent cohort = findPermanent(player1, "Mudbrawler Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(1);
    }

    @Test
    @DisplayName("No boost with a non-red creature")
    void noBoostWithNonRedCreature() {
        harness.addToBattlefield(player1, new MudbrawlerCohort());
        harness.addToBattlefield(player1, new GrizzlyBears()); // green

        Permanent cohort = findPermanent(player1, "Mudbrawler Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +1/+1 when controller controls another red creature")
    void boostWithAnotherRedCreature() {
        harness.addToBattlefield(player1, new MudbrawlerCohort());
        harness.addToBattlefield(player1, new HillGiant()); // red

        Permanent cohort = findPermanent(player1, "Mudbrawler Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Cohorts see each other as another red creature and both get +1/+1")
    void twoCohortsBoostEachOther() {
        harness.addToBattlefield(player1, new MudbrawlerCohort());
        harness.addToBattlefield(player1, new MudbrawlerCohort());

        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mudbrawler Cohort"))
                .forEach(p -> {
                    assertThat(gqs.getEffectivePower(gd, p)).isEqualTo(2);
                    assertThat(gqs.getEffectiveToughness(gd, p)).isEqualTo(2);
                });
    }

    @Test
    @DisplayName("Opponent's red creature does not grant the boost")
    void opponentRedCreatureDoesNotCount() {
        harness.addToBattlefield(player1, new MudbrawlerCohort());
        harness.addToBattlefield(player2, new HillGiant()); // red, but opponent's

        Permanent cohort = findPermanent(player1, "Mudbrawler Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(1);
    }

    @Test
    @DisplayName("Loses boost when the other red creature leaves the battlefield")
    void losesBoostWhenRedCreatureLeaves() {
        harness.addToBattlefield(player1, new MudbrawlerCohort());
        harness.addToBattlefield(player1, new HillGiant());

        Permanent cohort = findPermanent(player1, "Mudbrawler Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Hill Giant"));

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(1);
    }
}
