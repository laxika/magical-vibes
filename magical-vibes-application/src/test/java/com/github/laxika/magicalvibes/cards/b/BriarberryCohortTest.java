package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Cinderbones;
import com.github.laxika.magicalvibes.cards.p.PucasMischief;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BriarberryCohort.class, Cinderbones.class, PucasMischief.class})
class BriarberryCohortTest extends BaseCardTest {

    @Test
    @DisplayName("Base 1/1 when no other blue creature is controlled")
    void noBoostWhenAlone() {
        Permanent cohort = addCreatureReady(player1, new BriarberryCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(1);
    }

    @Test
    @DisplayName("No boost with a non-blue creature")
    void noBoostWithNonBlueCreature() {
        Permanent cohort = addCreatureReady(player1, new BriarberryCohort());
        addCreatureReady(player1, new Cinderbones());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(1);
    }

    @Test
    @DisplayName("No boost with a blue non-creature permanent")
    void noBoostWithBlueNonCreature() {
        Permanent cohort = addCreatureReady(player1, new BriarberryCohort());
        harness.addToBattlefield(player1, new PucasMischief());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +1/+1 when controller controls another blue creature")
    void boostWithAnotherBlueCreature() {
        Permanent cohort = addCreatureReady(player1, new BriarberryCohort());
        addCreatureReady(player1, new BriarberryCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost does not stack with multiple blue creatures")
    void boostDoesNotStack() {
        Permanent cohort = addCreatureReady(player1, new BriarberryCohort());
        addCreatureReady(player1, new BriarberryCohort());
        addCreatureReady(player1, new BriarberryCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Briarberry Cohorts boost each other (each is another blue creature)")
    void twoCohortsBoostEachOther() {
        addCreatureReady(player1, new BriarberryCohort());
        addCreatureReady(player1, new BriarberryCohort());

        for (Permanent cohort : findPermanents(player1, "Briarberry Cohort")) {
            assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Opponent's blue creature does not grant the boost")
    void opponentBlueCreatureDoesNotCount() {
        Permanent cohort = addCreatureReady(player1, new BriarberryCohort());
        addCreatureReady(player2, new BriarberryCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(1);
    }

    @Test
    @DisplayName("Loses boost when the other blue creature leaves the battlefield")
    void losesBoostWhenBlueCreatureLeaves() {
        Permanent cohort = addCreatureReady(player1, new BriarberryCohort());
        Permanent otherCohort = harness.addToBattlefieldAndReturn(player1, new BriarberryCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(otherCohort);

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Briarberry Cohort")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new BriarberryCohort());
        addCreatureReady(player2, new Cinderbones());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

}
