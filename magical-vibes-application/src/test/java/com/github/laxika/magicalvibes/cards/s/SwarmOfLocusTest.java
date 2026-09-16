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
