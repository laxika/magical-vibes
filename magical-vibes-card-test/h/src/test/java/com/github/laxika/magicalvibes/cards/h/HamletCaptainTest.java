package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HamletCaptain.class, WalkingCorpse.class, ThinkTwice.class})
class HamletCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Hamlet Captain boosts other Humans +1/+1")
    void attackBoostsOtherHumans() {
        // Hamlet Captain (Human Warrior) at index 0
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new HamletCaptain());
        captain.setSummoningSick(false);

        // Another Hamlet Captain (Human Warrior) at index 1
        Permanent human = harness.addToBattlefieldAndReturn(player1, new HamletCaptain());
        human.setSummoningSick(false);

        // Give player2 a playable instant to prevent auto-pass
        harness.setHand(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        // Resolve the trigger
        harness.passBothPriorities();

        // The other Hamlet Captain (2/2) should now be 3/3
        assertThat(PermanentGrants.powerBoostOf(gd, human)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, human)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking with Hamlet Captain does not boost itself")
    void attackDoesNotBoostSelf() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new HamletCaptain());
        captain.setSummoningSick(false);

        // Give player2 a playable instant to prevent auto-pass
        harness.setHand(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        // Hamlet Captain should NOT get the boost (it says "other Humans")
        assertThat(PermanentGrants.powerBoostOf(gd, captain)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, captain)).isEqualTo(0);
    }

    @Test
    @DisplayName("Attacking with Hamlet Captain does not boost non-Human creatures")
    void attackDoesNotBoostNonHumans() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new HamletCaptain());
        captain.setSummoningSick(false);

        // Walking Corpse is a Zombie, not a Human
        Permanent corpse = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        corpse.setSummoningSick(false);

        // Give player2 a playable instant to prevent auto-pass
        harness.setHand(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        // Walking Corpse should not get the boost
        assertThat(PermanentGrants.powerBoostOf(gd, corpse)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, corpse)).isEqualTo(0);
    }

    @Test
    @DisplayName("Blocking with Hamlet Captain boosts other Humans +1/+1")
    void blockBoostsOtherHumans() {
        // Hamlet Captain on player2's side (blocker)
        Permanent captain = harness.addToBattlefieldAndReturn(player2, new HamletCaptain());
        captain.setSummoningSick(false);

        // Another Human on player2's side
        Permanent human = harness.addToBattlefieldAndReturn(player2, new HamletCaptain());
        human.setSummoningSick(false);

        // Attacker on player1's side
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        harness.setHand(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        // Hamlet Captain (index 0 on player2's battlefield) blocks attacker (index 0 on player1's battlefield)
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // The other Hamlet Captain should get +1/+1
        assertThat(PermanentGrants.powerBoostOf(gd, human)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, human)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, captain)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, captain)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
    }

    @Test
    @CardUsed({HighGround.class})
    @DisplayName("Blocking two creatures triggers once")
    void blockingTwoCreaturesTriggersOnce() {
        addCreatureReady(player2, new HamletCaptain());
        Permanent human = addCreatureReady(player2, new HamletCaptain());
        harness.addToBattlefield(player2, new HighGround());
        addCreatureReady(player1, new WalkingCorpse()).setAttacking(true);
        addCreatureReady(player1, new WalkingCorpse()).setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(PermanentGrants.powerBoostOf(gd, human)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, human)).isEqualTo(1);
    }

    @Test
    @DisplayName("The attack trigger affects only Humans controlled at resolution")
    void attackBoostUsesResolutionBattlefield() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new HamletCaptain());
        captain.setSummoningSick(false);
        Permanent opponentHuman = harness.addToBattlefieldAndReturn(player2, new HamletCaptain());
        harness.setHand(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        Permanent humanBeforeResolution = harness.addToBattlefieldAndReturn(player1, new HamletCaptain());
        harness.passBothPriorities();
        Permanent humanAfterResolution = harness.addToBattlefieldAndReturn(player1, new HamletCaptain());

        assertThat(PermanentGrants.powerBoostOf(gd, humanBeforeResolution)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, humanBeforeResolution)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, humanAfterResolution)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, humanAfterResolution)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, opponentHuman)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opponentHuman)).isZero();
    }

    @Test
    @DisplayName("The trigger resolves after Hamlet Captain leaves the battlefield and expires at cleanup")
    void boostSurvivesSourceRemovalAndExpiresAtCleanup() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new HamletCaptain());
        captain.setSummoningSick(false);
        Permanent human = harness.addToBattlefieldAndReturn(player1, new HamletCaptain());
        harness.setHand(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(captain);
        gd.playerGraveyards.get(player1.getId()).add(captain.getCard());
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, human)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, human)).isEqualTo(1);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);
        assertThat(PermanentGrants.powerBoostOf(gd, human)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, human)).isZero();
    }
}
