package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FrostOgre;
import com.github.laxika.magicalvibes.cards.f.Frostling;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
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
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(attacker.getId()));
    }

    @Test
    @DisplayName("The blocked attacker is returned to its owner's hand at end of combat")
    void blockedAttackerReturnedToHand() {
        Permanent attacker = addCreatureReady(player1, new Frostling());
        attacker.setAttacking(true);
        addCreatureReady(player2, new KaijinOfTheVanishingTouch());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

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

        harness.passBothPriorities();
        harness.passBothPriorities();

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

        harness.passBothPriorities();

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
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(attacker.getId()));

        harness.passBothPriorities();
        harness.assertInHand(player1, "Frostling");
    }
}
