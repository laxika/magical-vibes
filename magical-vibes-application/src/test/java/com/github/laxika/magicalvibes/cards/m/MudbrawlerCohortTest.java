package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RageReflection;
import com.github.laxika.magicalvibes.cards.z.ZealousGuardian;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MudbrawlerCohort.class, RageReflection.class, ZealousGuardian.class})
class MudbrawlerCohortTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack immediately after resolving even without another red creature")
    void canAttackTheTurnItEnters() {
        MudbrawlerCohort card = new MudbrawlerCohort();
        harness.castFromHand(player1, card, "{1}{R}");
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));

        Permanent cohort = findPermanent(player1, "Mudbrawler Cohort");
        assertThat(cohort.getOriginalCard()).isSameAs(card);
        assertThat(cohort.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Multiple other red creatures grant only one +1/+1 bonus")
    void bonusDoesNotScaleWithNumberOfRedCreatures() {
        Permanent cohort = harness.addToBattlefieldAndReturn(player1, new MudbrawlerCohort());
        harness.addToBattlefield(player1, new MudbrawlerCohort());
        harness.addToBattlefield(player1, new MudbrawlerCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(2);
    }

    @Test
    @DisplayName("Base 1/1 when no other red creature is controlled")
    void noBoostWhenAlone() {
        Permanent cohort = harness.addToBattlefieldAndReturn(player1, new MudbrawlerCohort());
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(1);
    }

    @Test
    @DisplayName("No boost with a non-red creature")
    void noBoostWithNonRedCreature() {
        Permanent cohort = harness.addToBattlefieldAndReturn(player1, new MudbrawlerCohort());
        harness.addToBattlefield(player1, new ZealousGuardian());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(1);
    }

    @Test
    @DisplayName("No boost with a red noncreature permanent")
    void noBoostWithRedNonCreaturePermanent() {
        Permanent cohort = harness.addToBattlefieldAndReturn(player1, new MudbrawlerCohort());
        harness.addToBattlefield(player1, new RageReflection());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +1/+1 when controller controls another red creature")
    void boostWithAnotherRedCreature() {
        Permanent cohort = harness.addToBattlefieldAndReturn(player1, new MudbrawlerCohort());
        harness.addToBattlefield(player1, new MudbrawlerCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Cohorts see each other as another red creature and both get +1/+1")
    void twoCohortsBoostEachOther() {
        Permanent firstCohort = harness.addToBattlefieldAndReturn(player1, new MudbrawlerCohort());
        Permanent secondCohort = harness.addToBattlefieldAndReturn(player1, new MudbrawlerCohort());

        assertThat(gqs.getEffectivePower(gd, firstCohort)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstCohort)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCohort)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondCohort)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's red creature does not grant the boost")
    void opponentRedCreatureDoesNotCount() {
        Permanent cohort = harness.addToBattlefieldAndReturn(player1, new MudbrawlerCohort());
        harness.addToBattlefield(player2, new MudbrawlerCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(1);
    }

    @Test
    @DisplayName("Loses boost when the other red creature leaves the battlefield")
    void losesBoostWhenRedCreatureLeaves() {
        Permanent cohort = harness.addToBattlefieldAndReturn(player1, new MudbrawlerCohort());
        Permanent otherCohort = harness.addToBattlefieldAndReturn(player1, new MudbrawlerCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(otherCohort);

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(1);
    }
}
