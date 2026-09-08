package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.k.KjeldoranDead;
import com.github.laxika.magicalvibes.cards.k.KjeldoranSkycaptain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LimDLsCohort.class, BalduvianBears.class, KjeldoranDead.class, KjeldoranSkycaptain.class})
class LimDLsCohortTest extends BaseCardTest {

    @Test
    @DisplayName("When the Cohort blocks a creature, that attacker can't be regenerated this turn")
    void blocksCreatureMarksCantRegenerate() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new LimDLsCohort());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(attacker.isCantRegenerateThisTurn()).isTrue();
    }

    @Test
    @DisplayName("When the Cohort becomes blocked by a creature, that blocker can't be regenerated this turn")
    void becomesBlockedMarksCantRegenerate() {
        Permanent cohort = addCreatureReady(player1, new LimDLsCohort());
        cohort.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(blocker.isCantRegenerateThisTurn()).isTrue();
    }

    @Test
    @DisplayName("When the Cohort becomes blocked by multiple creatures, each blocker can't be regenerated this turn")
    void becomesBlockedMarksEachBlocker() {
        Permanent cohort = addCreatureReady(player1, new LimDLsCohort());
        cohort.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new BalduvianBears());
        Permanent secondBlocker = addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(firstBlocker.isCantRegenerateThisTurn()).isTrue();
        assertThat(secondBlocker.isCantRegenerateThisTurn()).isTrue();
    }

    @Test
    @DisplayName("A creature marked by the Cohort cannot be saved by a regeneration shield")
    void markedCreatureCannotBeRegenerated() {
        Permanent cohort = addCreatureReady(player1, new LimDLsCohort());
        cohort.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new KjeldoranDead());
        blocker.setRegenerationShield(1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        blocker.setMarkedDamage(10);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Kjeldoran Dead");
        harness.assertInGraveyard(player2, "Kjeldoran Dead");
    }

    @Test
    @DisplayName("A Cohort blocking one member of a band also marks the other band member")
    void blocksEveryCreatureInAttackingBand() {
        Permanent bandedCreature = addCreatureReady(player1, new KjeldoranSkycaptain());
        Permanent otherBandMember = addCreatureReady(player1, new BalduvianBears());
        Permanent cohort = addCreatureReady(player2, new LimDLsCohort());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1)));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        assertThat(otherBandMember.isCantRegenerateThisTurn()).isTrue();
        assertThat(bandedCreature.isCantRegenerateThisTurn()).isTrue();
        assertThat(cohort.getBlockingTargetIds()).containsExactlyInAnyOrder(
                otherBandMember.getId(), bandedCreature.getId());
    }

    @Test
    @DisplayName("A Cohort that becomes blocked as a band member marks its blocker")
    void becomesBlockedAsBandMemberMarksBlocker() {
        Permanent cohort = addCreatureReady(player1, new LimDLsCohort());
        Permanent bandedCreature = addCreatureReady(player1, new KjeldoranSkycaptain());
        Permanent blocker = addCreatureReady(player2, new KjeldoranSkycaptain());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1)));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        assertThat(blocker.isCantRegenerateThisTurn()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).containsExactlyInAnyOrder(
                cohort.getId(), bandedCreature.getId());
    }

    @Test
    @DisplayName("The can't-be-regenerated mark clears during end-of-turn cleanup")
    void markClearsAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new LimDLsCohort());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        assertThat(attacker.isCantRegenerateThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances END -> CLEANUP

        assertThat(attacker.isCantRegenerateThisTurn()).isFalse();
    }
}
