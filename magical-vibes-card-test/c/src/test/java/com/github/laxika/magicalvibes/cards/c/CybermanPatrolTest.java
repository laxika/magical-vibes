package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CybermanPatrol.class, Memnite.class, GrizzlyBears.class})
class CybermanPatrolTest extends BaseCardTest {

    @Test
    void artifactCreaturesYouControlGainAfflictThree() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new CybermanPatrol());
        addCreatureReady(player1, new Memnite());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(1));
        declareBlockers(0, 1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void nonArtifactCreaturesDoNotGainAfflict() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new CybermanPatrol());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new Memnite());

        declareAttackers(List.of(1));
        declareBlockers(0, 1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void declareBlockers(int blockerIndex, int attackerIndex) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }

    @Test
    void patrolGrantsAfflictToItself() {
        addCreatureReady(player1, new CybermanPatrol());
        addCreatureReady(player2, new CybermanPatrol());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    void afflictTriggersOnlyOnceWhenSeveralCreaturesBlock() {
        addCreatureReady(player1, new CybermanPatrol());
        addCreatureReady(player2, new CybermanPatrol());
        addCreatureReady(player2, new CybermanPatrol());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void multiplePatrolsGrantSeparatelyResolvingAfflictAbilities() {
        addCreatureReady(player1, new CybermanPatrol());
        addCreatureReady(player1, new CybermanPatrol());
        addCreatureReady(player2, new CybermanPatrol());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        harness.assertLife(player2, 14);
    }

    @Test
    void opponentsArtifactCreaturesDoNotReceiveAfflict() {
        addCreatureReady(player1, new CybermanPatrol());
        addCreatureReady(player2, new Memnite());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }
}
