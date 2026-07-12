package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Deathgazer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AshenmoorCohortTest extends BaseCardTest {

    @Test
    @DisplayName("Base 4/3 when no other black creature is controlled")
    void noBoostWhenAlone() {
        harness.addToBattlefield(player1, new AshenmoorCohort());

        Permanent cohort = findPermanent(player1, "Ashenmoor Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(3);
    }

    @Test
    @DisplayName("No boost with a non-black creature")
    void noBoostWithNonBlackCreature() {
        harness.addToBattlefield(player1, new AshenmoorCohort());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent cohort = findPermanent(player1, "Ashenmoor Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 when controller controls another black creature")
    void boostWithAnotherBlackCreature() {
        harness.addToBattlefield(player1, new AshenmoorCohort());
        harness.addToBattlefield(player1, new Deathgazer());

        Permanent cohort = findPermanent(player1, "Ashenmoor Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent's black creature does not grant the boost")
    void opponentBlackCreatureDoesNotCount() {
        harness.addToBattlefield(player1, new AshenmoorCohort());
        harness.addToBattlefield(player2, new Deathgazer());

        Permanent cohort = findPermanent(player1, "Ashenmoor Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(3);
    }

    @Test
    @DisplayName("Loses boost when the other black creature leaves the battlefield")
    void losesBoostWhenBlackCreatureLeaves() {
        harness.addToBattlefield(player1, new AshenmoorCohort());
        harness.addToBattlefield(player1, new Deathgazer());

        Permanent cohort = findPermanent(player1, "Ashenmoor Cohort");
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Deathgazer"));

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(3);
    }
}
