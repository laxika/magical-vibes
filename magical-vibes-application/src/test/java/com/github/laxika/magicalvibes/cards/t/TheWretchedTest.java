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

class TheWretchedTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked schedules each blocker for an end-of-combat control gain")
    void becomesBlockedSchedulesControlGain() {
        Permanent wretched = addCreatureReady(player1, new TheWretched());
        wretched.setAttacking(true);
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        declareSingleBlocker();

        assertThat(gd.getDelayedActions(GainControlOfPermanentAtEndOfCombat.class))
                .anyMatch(a -> a.permanentId().equals(bear.getId())
                        && a.newControllerId().equals(player1.getId())
                        && a.sourcePermanentId().equals(wretched.getId()));
    }

    @Test
    @DisplayName("At end of combat, controller gains control of all creatures blocking The Wretched")
    void gainsControlOfAllBlockersAtEndOfCombat() {
        Permanent wretched = addCreatureReady(player1, new TheWretched());
        wretched.setAttacking(true);
        Permanent bear1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent bear2 = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        // Not yet stolen while combat is ongoing.
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear1, bear2);

        leaveEndOfCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear1, bear2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear1, bear2);
    }

    @Test
    @DisplayName("Control ends when The Wretched leaves the battlefield")
    void controlEndsWhenSourceLeaves() {
        Permanent wretched = addCreatureReady(player1, new TheWretched());
        wretched.setAttacking(true);
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        declareSingleBlocker();
        leaveEndOfCombat();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);

        // The Wretched leaves; its "for as long as you control this creature" control ends.
        gd.playerBattlefields.get(player1.getId()).remove(wretched);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
    }

    @Test
    @DisplayName("Does nothing when The Wretched is not blocked")
    void noControlWhenNotBlocked() {
        addCreatureReady(player1, new TheWretched());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        leaveEndOfCombat();

        assertThat(gd.hasDelayedAction(GainControlOfPermanentAtEndOfCombat.class)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
    }

    private void declareSingleBlocker() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
    }

    private void leaveEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
