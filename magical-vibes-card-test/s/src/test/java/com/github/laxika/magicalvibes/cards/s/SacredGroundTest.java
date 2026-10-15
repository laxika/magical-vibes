package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BottomlessPit;
import com.github.laxika.magicalvibes.cards.b.BraidsCabalMinion;
import com.github.laxika.magicalvibes.cards.d.Donate;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.r.Reclaim;
import com.github.laxika.magicalvibes.cards.r.Ruination;
import com.github.laxika.magicalvibes.cards.v.VerdantTouch;
import com.github.laxika.magicalvibes.cards.v.VolrathsStronghold;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({SacredGround.class, Ruination.class, VolrathsStronghold.class, VerdantTouch.class, StrongholdAssassin.class, BottomlessPit.class, BraidsCabalMinion.class, Donate.class, ShardVolley.class, Reclaim.class, Millstone.class, Shock.class})
class SacredGroundTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's spell destroying your land returns it to the battlefield")
    void opponentDestroysYourLandReturnsIt() {
        harness.addToBattlefield(player1, new SacredGround());
        harness.addToBattlefield(player1, new VolrathsStronghold());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new Ruination(), "{3}{R}");
        resolveAllTriggers();

        // Sacred Ground returned the land to its owner's battlefield.
        harness.assertOnBattlefield(player1, "Volrath's Stronghold");
        harness.assertNotInGraveyard(player1, "Volrath's Stronghold");
    }

    @Test
    @DisplayName("A mass-removal spell returns only your land to your battlefield")
    void massRemovalReturnsOnlyYourLand() {
        harness.addToBattlefield(player1, new SacredGround());
        harness.addToBattlefield(player1, new VolrathsStronghold());
        harness.addToBattlefield(player2, new VolrathsStronghold());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new Ruination(), "{3}{R}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Volrath's Stronghold");
        harness.assertNotInGraveyard(player1, "Volrath's Stronghold");
        harness.assertNotOnBattlefield(player2, "Volrath's Stronghold");
        harness.assertInGraveyard(player2, "Volrath's Stronghold");
    }

    @Test
    @DisplayName("Your own spell destroying your own land does not trigger Sacred Ground")
    void ownSpellDestroyingOwnLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new SacredGround());
        harness.addToBattlefield(player1, new VolrathsStronghold());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new Ruination(), "{3}{R}");
        resolveAllTriggers();

        // Cause is controlled by the land's owner, so the land stays in the graveyard.
        harness.assertNotOnBattlefield(player1, "Volrath's Stronghold");
        harness.assertInGraveyard(player1, "Volrath's Stronghold");
    }

    @Test
    @DisplayName("Opponent destroying their own land does not trigger your Sacred Ground")
    void opponentDestroyingTheirOwnLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new SacredGround());
        harness.addToBattlefield(player2, new VolrathsStronghold());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new Ruination(), "{3}{R}");
        resolveAllTriggers();

        // The land went to the opponent's graveyard, not the Sacred Ground controller's, so no trigger.
        harness.assertNotOnBattlefield(player2, "Volrath's Stronghold");
        harness.assertInGraveyard(player2, "Volrath's Stronghold");
    }

    @Test
    @DisplayName("Opponent's ability destroying your land returns it to the battlefield")
    void opponentAbilityDestroyingYourLandReturnsIt() {
        harness.addToBattlefield(player1, new SacredGround());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());

        harness.setHand(player1, List.of(new VerdantTouch()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, land.getId());
        resolveAllTriggers();

        Permanent assassin = addCreatureReady(player2, new StrongholdAssassin());
        Permanent fodder = addCreatureReady(player2, new StrongholdAssassin());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int assassinIndex = gd.playerBattlefields.get(player2.getId()).indexOf(assassin);
        harness.activateAbility(player2, assassinIndex, 0, null, land.getId());
        harness.handlePermanentChosen(player2, fodder.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Volrath's Stronghold");
        harness.assertNotInGraveyard(player1, "Volrath's Stronghold");
    }

    @Test
    @DisplayName("An opponent's sacrifice ability returns the land chosen for sacrifice")
    void opponentAbilitySacrificingYourLandReturnsIt() {
        harness.addToBattlefield(player1, new SacredGround());
        harness.addToBattlefield(player2, new BraidsCabalMinion());
        harness.addToBattlefield(player1, new StrongholdAssassin());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(land.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Volrath's Stronghold");
        harness.assertNotInGraveyard(player1, "Volrath's Stronghold");
        harness.assertOnBattlefield(player1, "Stronghold Assassin");
    }

    @Test
    @DisplayName("A land discarded from hand is not returned by Sacred Ground")
    void landDiscardedFromHandDoesNotTrigger() {
        harness.addToBattlefield(player1, new SacredGround());
        harness.addToBattlefield(player2, new BottomlessPit());
        harness.setHand(player1, List.of(new VolrathsStronghold()));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Volrath's Stronghold");
        harness.assertInGraveyard(player1, "Volrath's Stronghold");
    }

    @Test
    @DisplayName("Sacrificing your land to pay an opponent's spell cost does not trigger Sacred Ground")
    void opponentsAdditionalCostSacrificeDoesNotReturnLand() {
        harness.addToBattlefield(player1, new SacredGround());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());

        harness.setHand(player1, List.of(new Donate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, List.of(player2.getId(), land.getId()));
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new ShardVolley()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstantWithSacrifice(player2, 0, player1.getId(), land.getId());
        harness.assertInGraveyard(player1, "Volrath's Stronghold");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Volrath's Stronghold");
        harness.assertNotOnBattlefield(player2, "Volrath's Stronghold");
        harness.assertInGraveyard(player1, "Volrath's Stronghold");
    }

    @Test
    @DisplayName("A land that leaves the graveyard and is milled back is not returned by the old trigger")
    void landLeavingAndReenteringGraveyardIsNotReturned() {
        harness.addToBattlefield(player1, new SacredGround());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());
        Permanent millstone = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.setLibrary(player1, List.of(new Reclaim()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new Ruination(), "{3}{R}");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Volrath's Stronghold");

        harness.setHand(player1, List.of(new Reclaim()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, land.getCard().getId());
        harness.passBothPriorities();
        harness.assertNotInGraveyard(player1, "Volrath's Stronghold");

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        int millstoneIndex = gd.playerBattlefields.get(player2.getId()).indexOf(millstone);
        harness.activateAbility(player2, millstoneIndex, null, player1.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Volrath's Stronghold");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Volrath's Stronghold");
        harness.assertInGraveyard(player1, "Volrath's Stronghold");
    }

    @Test
    @DisplayName("Lethal spell damage to an animated land does not trigger Sacred Ground")
    void animatedLandDyingToLethalDamageIsNotReturned() {
        harness.addToBattlefield(player1, new SacredGround());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());
        harness.setHand(player1, List.of(new VerdantTouch()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, land.getId());
        resolveAllTriggers();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, land.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Volrath's Stronghold");
        harness.assertInGraveyard(player1, "Volrath's Stronghold");
    }
}
