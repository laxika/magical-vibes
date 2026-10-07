package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CrawGiant;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.w.WallOfEarth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheWretched.class, CrawGiant.class, WallOfEarth.class, RayOfCommand.class})
class TheWretchedTest extends BaseCardTest {

    @Test
    @DisplayName("End-of-combat ability is not created during blocker declaration")
    void doesNotTriggerDuringBlockerDeclaration() {
        addCreatureReady(player1, new TheWretched());
        addCreatureReady(player2, new WallOfEarth());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard() instanceof TheWretched);
    }

    @Test
    @DisplayName("At end of combat, controller gains control of all creatures blocking The Wretched")
    void gainsControlOfAllBlockersAtEndOfCombat() {
        addCreatureReady(player1, new TheWretched());
        Permanent blocker1 = addCreatureReady(player2, new WallOfEarth());
        Permanent blocker2 = addCreatureReady(player2, new WallOfEarth());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            resolveCombat();
            harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker1.getId(), 2));
        });

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
        Permanent blocker = addCreatureReady(player2, new WallOfEarth());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        leaveEndOfCombat();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);

        // The Wretched leaves; its "for as long as you control this creature" control ends.
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, wretched));
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Does not gain control if The Wretched leaves before its ability resolves")
    void doesNotGainControlIfSourceLeavesBeforeAbilityResolves() {
        Permanent wretched = addCreatureReady(player1, new TheWretched());
        Permanent blocker = addCreatureReady(player2, new WallOfEarth());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, wretched));
        leaveEndOfCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Does nothing when The Wretched is not blocked")
    void noControlWhenNotBlocked() {
        addCreatureReady(player1, new TheWretched());
        Permanent blocker = addCreatureReady(player2, new WallOfEarth());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        leaveEndOfCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Does not gain control of a blocker after The Wretched regenerates")
    void doesNotGainControlOfBlockerAfterSourceRegenerates() {
        Permanent wretched = addCreatureReady(player1, new TheWretched());
        wretched.setRegenerationShield(1);
        Permanent blocker = addCreatureReady(player2, new CrawGiant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        leaveEndOfCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Losing control of The Wretched ends control of its blockers permanently")
    void controlDoesNotResumeWhenSourceReturns() {
        Permanent wretched = addCreatureReady(player1, new TheWretched());
        Permanent blocker = addCreatureReady(player2, new WallOfEarth());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        leaveEndOfCombat();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);

        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player2, 0, wretched.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wretched, blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blocker);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wretched).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Does not gain control when The Wretched changes controller before resolution")
    void noControlWhenSourceChangesControllerBeforeResolution() {
        Permanent wretched = addCreatureReady(player1, new TheWretched());
        Permanent blocker = addCreatureReady(player2, new WallOfEarth());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() instanceof TheWretched);

        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player2, 0, wretched.getId());
        leaveEndOfCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wretched, blocker);
    }

    private void leaveEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
