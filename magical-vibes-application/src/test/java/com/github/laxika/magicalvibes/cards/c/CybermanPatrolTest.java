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
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
