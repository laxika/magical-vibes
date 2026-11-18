package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.c.Cloudpost;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwarmOfLocus.class, Cloudpost.class})
class SwarmOfLocusTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each Locus its controller controls, including itself")
    void boostCountsControlledLoci() {
        Permanent swarm = addCreatureReady(player1, new SwarmOfLocus());
        addCreatureReady(player1, new SwarmOfLocus());
        harness.addToBattlefield(player1, new Cloudpost());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, swarm)).isEqualTo(3);
        assertThat(PermanentGrants.toughnessBoostOf(gd, swarm)).isZero();
    }

    @Test
    @DisplayName("Ignores Loci controlled by an opponent")
    void boostIgnoresOpponentsLoci() {
        Permanent swarm = addCreatureReady(player1, new SwarmOfLocus());
        harness.addToBattlefield(player2, new SwarmOfLocus());
        harness.addToBattlefield(player2, new Cloudpost());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, swarm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts Loci when the attack trigger resolves")
    void countsLociAtResolution() {
        Permanent swarm = addCreatureReady(player1, new SwarmOfLocus());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        assertThat(PermanentGrants.powerBoostOf(gd, swarm)).isZero();
        harness.addToBattlefield(player1, new Cloudpost());

        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, swarm)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, swarm)).isZero();
    }

    @Test
    @DisplayName("The resolved boost does not change when another Locus enters")
    void resolvedBoostIsFixed() {
        Permanent swarm = addCreatureReady(player1, new SwarmOfLocus());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(PermanentGrants.powerBoostOf(gd, swarm)).isEqualTo(1);

        harness.addToBattlefield(player1, new Cloudpost());

        assertThat(PermanentGrants.powerBoostOf(gd, swarm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each attacking Swarm boosts only itself")
    void multipleAttackersResolveIndependentBoosts() {
        Permanent first = addCreatureReady(player1, new SwarmOfLocus());
        Permanent second = addCreatureReady(player1, new SwarmOfLocus());
        Permanent nonattacker = addCreatureReady(player1, new SwarmOfLocus());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(3);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(3);
        assertThat(PermanentGrants.powerBoostOf(gd, nonattacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isZero();
    }

    @Test
    @DisplayName("The attack boost expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent swarm = addCreatureReady(player1, new SwarmOfLocus());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(PermanentGrants.powerBoostOf(gd, swarm)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, swarm)).isZero();
    }
}
