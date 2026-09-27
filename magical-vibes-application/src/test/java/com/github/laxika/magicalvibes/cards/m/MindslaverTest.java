package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mindslaver.class, Ornithopter.class})
class MindslaverTest extends BaseCardTest {

    private void advanceTurn(Player activePlayer) {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(activePlayer, TurnStep.PRECOMBAT_MAIN);
    }

    // ===== Activation and delayed effect =====

    @Test
    @DisplayName("Activating Mindslaver sets pending turn control")
    void activationSetsPendingControl() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Mindslaver());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID opponentId = player2.getId();
        harness.activateAbility(player1, 0, null, opponentId);
        harness.passBothPriorities();

        assertThat(gd.pendingTurnControl).containsEntry(opponentId, player1.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Mindslaver goes to graveyard after activation (sacrifice cost)")
    void sacrificeGoesToGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Mindslaver());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID opponentId = player2.getId();
        harness.activateAbility(player1, 0, null, opponentId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mindslaver");
        harness.assertInGraveyard(player1, "Mindslaver");
    }

    // ===== Turn control activation =====

    @Test
    @DisplayName("Mind control activates when controlled player's turn begins")
    void turnControlActivatesOnOpponentTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Mindslaver());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID opponentId = player2.getId();
        harness.activateAbility(player1, 0, null, opponentId);
        harness.passBothPriorities();

        // Advance to opponent's turn
        advanceTurn(player2);

        assertThat(gd.activePlayerId).isEqualTo(opponentId);
        assertThat(gd.mindControlledPlayerId).isEqualTo(opponentId);
        assertThat(gd.mindControllerPlayerId).isEqualTo(player1.getId());
        // Pending control should be consumed
        assertThat(gd.pendingTurnControl).isEmpty();
    }

    // ===== Controller can act for controlled player =====

    @Test
    @DisplayName("Controller can pass priority on behalf of controlled player")
    void controllerCanPassPriority() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Mindslaver());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Advance to opponent's turn
        advanceTurn(player2);

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.mindControlledPlayerId).isEqualTo(player2.getId());

        // Controller (player1) passes priority — acts as controlled player (player2)
        // Since player2 is active player with priority, controller's pass maps to player2
        gs.passPriority(gd, player1);
        // player1 also passes as themselves (non-active player)
        gs.passPriority(gd, player1);
        // If we got here without error, the controller successfully passed priority for the controlled player
    }

    @Test
    @DisplayName("Controller can play cards from controlled player's hand")
    void controllerCanPlayControlledPlayerCards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Mindslaver());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Advance to player2's turn
        advanceTurn(player2);

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.mindControlledPlayerId).isEqualTo(player2.getId());

        // Set player2's hand AFTER turn advancement (draw step adds cards)
        Card testCard = new Ornithopter();
        harness.setHand(player2, List.of(testCard));

        // Controller (player1) casts from controlled player's hand
        // Ornithopter costs {0}, so no mana needed
        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        // The creature should be on player2's battlefield (controlled player's resources)
        harness.assertOnBattlefield(player2, "Ornithopter");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    // ===== Turn control deactivation =====

    @Test
    @DisplayName("Mind control ends when controlled turn ends")
    void turnControlEndsAtTurnEnd() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Mindslaver());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Advance to player2's turn (mind control activates)
        advanceTurn(player2);
        assertThat(gd.mindControlledPlayerId).isEqualTo(player2.getId());

        // Advance past the controlled turn to player1's turn
        advanceTurn(player1);

        // Mind control should be cleared
        assertThat(gd.mindControlledPlayerId).isNull();
        assertThat(gd.mindControllerPlayerId).isNull();
    }

    // ===== Targeting and timing =====

    @Test
    @DisplayName("Can target self with Mindslaver (per rulings, does nothing meaningful)")
    void canTargetSelf() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Mindslaver());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID selfId = player1.getId();
        harness.activateAbility(player1, 0, null, selfId);
        harness.passBothPriorities();

        assertThat(gd.pendingTurnControl).containsEntry(selfId, player1.getId());
    }

    @Test
    @DisplayName("Can activate Mindslaver at instant speed (no sorcery restriction)")
    void canActivateAtInstantSpeed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addToBattlefield(player1, new Mindslaver());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID opponentId = player2.getId();
        harness.activateAbility(player1, 0, null, opponentId);
        harness.passBothPriorities();

        assertThat(gd.pendingTurnControl).containsEntry(opponentId, player1.getId());
    }
}
