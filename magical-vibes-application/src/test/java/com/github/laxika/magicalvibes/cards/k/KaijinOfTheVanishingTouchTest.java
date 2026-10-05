package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FrostOgre;
import com.github.laxika.magicalvibes.cards.f.Frostling;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedEndOfCombatTrigger;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaijinOfTheVanishingTouch.class, Frostling.class, FrostOgre.class, ImprisonedInTheMoon.class})
class KaijinOfTheVanishingTouchTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a creature schedules that attacker for an end-of-combat bounce")
    void blockingSchedulesReturnToHand() {
        Permanent attacker = addCreatureReady(player1, new Frostling());
        attacker.setAttacking(true);
        addCreatureReady(player2, new KaijinOfTheVanishingTouch());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Kaijin of the Vanishing Touch")
                        && se.getTargetId().equals(attacker.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedEndOfCombatTrigger.class))
                .anyMatch(a -> attacker.getId().equals(a.affectedPermanentId()));
    }

    @Test
    @DisplayName("The blocked attacker is returned to its owner's hand at end of combat")
    void blockedAttackerReturnedToHand() {
        Permanent attacker = addCreatureReady(player1, new Frostling());
        attacker.setAttacking(true);
        addCreatureReady(player2, new KaijinOfTheVanishingTouch());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertNotOnBattlefield(player1, "Frostling");
        harness.assertInHand(player1, "Frostling");
    }

    @Test
    @DisplayName("The blocked attacker still deals its combat damage before the bounce")
    void attackerStillDealsCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new FrostOgre());
        attacker.setAttacking(true);
        addCreatureReady(player2, new KaijinOfTheVanishingTouch());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        // 5 damage kills the 0/3 Kaijin, and the attacker is still bounced afterwards.
        harness.assertInGraveyard(player2, "Kaijin of the Vanishing Touch");
        harness.assertInHand(player1, "Frost Ogre");
    }

    @Test
    @DisplayName("An attacker that left the battlefield before end of combat is not returned")
    void attackerGoneBeforeEndOfCombatIsNotReturned() {
        Permanent attacker = addCreatureReady(player1, new Frostling());
        attacker.setAttacking(true);
        addCreatureReady(player2, new KaijinOfTheVanishingTouch());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(attacker.getId()));

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertNotInHand(player1, "Frostling");
    }

    @Test
    @DisplayName("The trigger still resolves if Kaijin leaves before it resolves")
    void triggerSurvivesSourceLeavingBattlefield() {
        Permanent attacker = addCreatureReady(player1, new Frostling());
        attacker.setAttacking(true);
        Permanent kaijin = addCreatureReady(player2, new KaijinOfTheVanishingTouch());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(kaijin.getId()));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertInHand(player1, "Frostling");
    }

    @Test
    @DisplayName("A blocked creature that becomes a noncreature before trigger resolution is still returned")
    void noncreatureBlockedAttackerIsStillReturned() {
        Permanent attacker = addCreatureReady(player1, new Frostling());
        attacker.setAttacking(true);
        addCreatureReady(player2, new KaijinOfTheVanishingTouch());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new ImprisonedInTheMoon());
        aura.setAttachedTo(attacker.getId());
        assertThat(gqs.isCreature(gd, attacker)).isFalse();

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedEndOfCombatTrigger.class))
                .anyMatch(a -> attacker.getId().equals(a.affectedPermanentId()));

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.assertInHand(player1, "Frostling");
    }

    @Test
    @DisplayName("The attacker remains on the battlefield until the end-of-combat trigger resolves")
    void returnUsesTheStackAtEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new Frostling());
        attacker.setAttacking(true);
        addCreatureReady(player2, new KaijinOfTheVanishingTouch());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player1, "Frostling");
        harness.assertNotInHand(player1, "Frostling");
        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && attacker.getId().equals(entry.getTargetId()));

        resolveAllTriggers();
        harness.assertInHand(player1, "Frostling");
    }

    @Test
    @DisplayName("A stolen attacker returns to its owner's hand rather than its controller's")
    void stolenAttackerReturnsToOwner() {
        Permanent attacker = addCreatureReady(player1, new Frostling());
        gd.stolenCreatures.put(attacker.getId(), player2.getId());
        attacker.setAttacking(true);
        addCreatureReady(player2, new KaijinOfTheVanishingTouch());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertNotInHand(player1, "Frostling");
        harness.assertInHand(player2, "Frostling");
    }

    @Test
    @DisplayName("Only the blocked attacker is returned, leaving an unblocked attacker in play")
    void unblockedAttackerIsNotReturned() {
        Permanent blocked = addCreatureReady(player1, new Frostling());
        blocked.setAttacking(true);
        Permanent unblocked = addCreatureReady(player1, new FrostOgre());
        unblocked.setAttacking(true);
        addCreatureReady(player2, new KaijinOfTheVanishingTouch());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertInHand(player1, "Frostling");
        harness.assertOnBattlefield(player1, "Frost Ogre");
        harness.assertNotInHand(player1, "Frost Ogre");
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Leaving and reentering the battlefield makes the attacker a new object")
    void returnedCardIsNotAffectedByTheOldDelayedTrigger() {
        Frostling card = new Frostling();
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
        addCreatureReady(player2, new KaijinOfTheVanishingTouch());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.getPermanentRemovalService().removePermanentToHand(gd, attacker);
        gd.playerHands.get(player1.getId()).remove(card);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, card);
        assertThat(returned.getId()).isNotEqualTo(attacker.getId());
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertOnBattlefield(player1, "Frostling");
        harness.assertNotInHand(player1, "Frostling");
    }

    @Test
    @DisplayName("A creature becoming a land after the delayed trigger is created is still returned")
    void changedCharacteristicsDoNotInvalidateTheDelayedReturn() {
        Permanent attacker = addCreatureReady(player1, new Frostling());
        attacker.setAttacking(true);
        addCreatureReady(player2, new KaijinOfTheVanishingTouch());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new ImprisonedInTheMoon());
        aura.setAttachedTo(attacker.getId());
        assertThat(gqs.isCreature(gd, attacker)).isFalse();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertInHand(player1, "Frostling");
        harness.assertInGraveyard(player2, "Imprisoned in the Moon");
    }
}
