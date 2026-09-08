package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfBone;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.GainControlOfPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheWretched.class, GrizzlyBears.class, CrawWurm.class, WallOfBone.class})
class TheWretchedTest extends BaseCardTest {

    @Test
    @DisplayName("End-of-combat ability is not created during blocker declaration")
    void doesNotTriggerDuringBlockerDeclaration() {
        addCreatureReady(player1, new TheWretched());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard() instanceof TheWretched);
    }

    @Test
    @DisplayName("At end of combat, controller gains control of all creatures blocking The Wretched")
    void gainsControlOfAllBlockersAtEndOfCombat() {
        addCreatureReady(player1, new TheWretched());
        Permanent blocker1 = addCreatureReady(player2, new WallOfBone());
        Permanent blocker2 = addCreatureReady(player2, new WallOfBone());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.END_OF_COMBAT));
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.END_OF_COMBAT));
        resolveAllTriggers();
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker1.getId(), 2));

        // Not yet stolen while combat is ongoing.
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker1, blocker2);

        leaveEndOfCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker1, blocker2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker1, blocker2);
    }

    @Test
    @DisplayName("Control ends when The Wretched leaves the battlefield")
    void controlEndsWhenSourceLeaves() {
        Permanent wretched = addCreatureReady(player1, new TheWretched());
        Permanent blocker = addCreatureReady(player2, new WallOfBone());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        leaveEndOfCombat();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);

        // The Wretched leaves; its "for as long as you control this creature" control ends.
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, wretched));
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Does nothing when The Wretched is not blocked")
    void noControlWhenNotBlocked() {
        addCreatureReady(player1, new TheWretched());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        leaveEndOfCombat();

        assertThat(gd.hasDelayedAction(GainControlOfPermanentAtEndOfCombat.class)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
    }

    @Test
    @DisplayName("Does not gain control of a blocker after The Wretched regenerates")
    void doesNotGainControlOfBlockerAfterSourceRegenerates() {
        Permanent wretched = addCreatureReady(player1, new TheWretched());
        wretched.setRegenerationShield(1);
        Permanent blocker = addCreatureReady(player2, new CrawWurm());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        leaveEndOfCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    private void leaveEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
