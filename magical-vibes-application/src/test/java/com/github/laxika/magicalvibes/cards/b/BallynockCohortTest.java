package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BallynockCohortTest extends BaseCardTest {

    @Test
    @DisplayName("Base 2/2 when no other white creature is controlled")
    void noBoostWhenAlone() {
        harness.addToBattlefield(player1, new BallynockCohort());

        Permanent cohort = findPermanent(player1, "Ballynock Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(2);
    }

    @Test
    @DisplayName("No boost with a non-white creature")
    void noBoostWithNonWhiteCreature() {
        harness.addToBattlefield(player1, new BallynockCohort());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent cohort = findPermanent(player1, "Ballynock Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +1/+1 when controlling another white creature")
    void boostWithAnotherWhiteCreature() {
        harness.addToBattlefield(player1, new BallynockCohort());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent cohort = findPermanent(player1, "Ballynock Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(3);
    }

    @Test
    @DisplayName("Self does not count — a lone Cohort is not 'another white creature'")
    void selfDoesNotCount() {
        harness.addToBattlefield(player1, new BallynockCohort());

        Permanent cohort = findPermanent(player1, "Ballynock Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's white creature does not grant the boost")
    void opponentWhiteCreatureDoesNotCount() {
        harness.addToBattlefield(player1, new BallynockCohort());
        harness.addToBattlefield(player2, new EliteVanguard());

        Permanent cohort = findPermanent(player1, "Ballynock Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(2);
    }

    @Test
    @DisplayName("Loses boost when the other white creature leaves the battlefield")
    void losesBoostWhenWhiteCreatureLeaves() {
        harness.addToBattlefield(player1, new BallynockCohort());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent cohort = findPermanent(player1, "Ballynock Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Elite Vanguard"));

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(2);
    }
}
