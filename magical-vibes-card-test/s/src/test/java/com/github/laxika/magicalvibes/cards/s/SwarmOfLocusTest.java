package com.github.laxika.magicalvibes.cards.s;

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

        assertThat(swarm.getPowerModifier()).isEqualTo(3);
        assertThat(swarm.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Ignores Loci controlled by an opponent")
    void boostIgnoresOpponentsLoci() {
        Permanent swarm = addCreatureReady(player1, new SwarmOfLocus());
        harness.addToBattlefield(player2, new SwarmOfLocus());
        harness.addToBattlefield(player2, new Cloudpost());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(swarm.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts Loci when the attack trigger resolves")
    void countsLociAtResolution() {
        Permanent swarm = addCreatureReady(player1, new SwarmOfLocus());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        assertThat(swarm.getPowerModifier()).isZero();
        harness.addToBattlefield(player1, new Cloudpost());

        resolveAllTriggers();

        assertThat(swarm.getPowerModifier()).isEqualTo(2);
        assertThat(swarm.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The resolved boost does not change when another Locus enters")
    void resolvedBoostIsFixed() {
        Permanent swarm = addCreatureReady(player1, new SwarmOfLocus());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(swarm.getPowerModifier()).isEqualTo(1);

        harness.addToBattlefield(player1, new Cloudpost());

        assertThat(swarm.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each attacking Swarm boosts only itself")
    void multipleAttackersResolveIndependentBoosts() {
        Permanent first = addCreatureReady(player1, new SwarmOfLocus());
        Permanent second = addCreatureReady(player1, new SwarmOfLocus());
        Permanent nonattacker = addCreatureReady(player1, new SwarmOfLocus());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(first.getPowerModifier()).isEqualTo(3);
        assertThat(second.getPowerModifier()).isEqualTo(3);
        assertThat(nonattacker.getPowerModifier()).isZero();
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The attack boost expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent swarm = addCreatureReady(player1, new SwarmOfLocus());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(swarm.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(swarm.getPowerModifier()).isZero();
    }
}
