package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BottomlessPit;
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

@CardUsed({SacredGround.class, Ruination.class, VolrathsStronghold.class, VerdantTouch.class,
        StrongholdAssassin.class, BottomlessPit.class})
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

        Permanent assassin = harness.addToBattlefieldAndReturn(player2, new StrongholdAssassin());
        Permanent fodder = harness.addToBattlefieldAndReturn(player2, new StrongholdAssassin());
        assassin.setSummoningSick(false);
        fodder.setSummoningSick(false);
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
}
