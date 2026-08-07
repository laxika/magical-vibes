package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.GainControlOfPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TolarianEntrancerTest extends BaseCardTest {

    @Test
    @DisplayName("At end of combat, controller gains control of each blocking creature")
    void gainsControlOfBlockersAtEndOfCombat() {
        Permanent entrancer = addCreatureReady(player1, new TolarianEntrancer());
        entrancer.setAttacking(true);
        Permanent bear1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent bear2 = addCreatureReady(player2, new GrizzlyBears());

        declareBlockers(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0));

        // Control changes only when combat ends.
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear1, bear2);

        leaveEndOfCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear1, bear2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear1, bear2);
    }

    @Test
    @DisplayName("Control is permanent and survives the Entrancer leaving the battlefield")
    void controlSurvivesSourceLeaving() {
        Permanent entrancer = addCreatureReady(player1, new TolarianEntrancer());
        entrancer.setAttacking(true);
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        declareBlockers(new BlockerAssignment(0, 0));
        leaveEndOfCombat();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);

        gd.playerBattlefields.get(player1.getId()).remove(entrancer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
    }

    @Test
    @DisplayName("Does nothing when the Entrancer is not blocked")
    void noControlWhenNotBlocked() {
        addCreatureReady(player1, new TolarianEntrancer());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        leaveEndOfCombat();

        assertThat(gd.hasDelayedAction(GainControlOfPermanentAtEndOfCombat.class)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
    }

    private void declareBlockers(BlockerAssignment... assignments) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(assignments));
        harness.passBothPriorities();
    }

    private void leaveEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
