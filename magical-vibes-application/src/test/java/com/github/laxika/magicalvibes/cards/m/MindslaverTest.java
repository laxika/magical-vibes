package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ControlTargetPlayerNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class MindslaverTest extends BaseCardTest {

    private void enableAutoStop() {
        Set<TurnStep> stops1 = ConcurrentHashMap.newKeySet();
        stops1.add(TurnStep.PRECOMBAT_MAIN);
        stops1.add(TurnStep.POSTCOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player1.getId(), stops1);
        Set<TurnStep> stops2 = ConcurrentHashMap.newKeySet();
        stops2.add(TurnStep.PRECOMBAT_MAIN);
        stops2.add(TurnStep.POSTCOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player2.getId(), stops2);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    // ===== Card structure =====

    @Test
    @DisplayName("Mindslaver has activated ability with tap and sacrifice cost, targets any player")
    void hasCorrectAbilityStructure() {
        Mindslaver card = new Mindslaver();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().get(0);
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{4}");
        assertThat(ability.getTimingRestriction()).isNull();
        assertThat(ability.getEffects())
                .hasSize(2)
                .anyMatch(e -> e instanceof SacrificeSelfCost)
                .anyMatch(e -> e instanceof ControlTargetPlayerNextTurnEffect);
        assertThat(ability.getTargetFilter()).isNull();
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
        enableAutoStop();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Mindslaver());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID opponentId = player2.getId();
        harness.activateAbility(player1, 0, null, opponentId);
        harness.passBothPriorities();

        // Advance to opponent's turn
        advanceTurn();

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
        enableAutoStop();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Mindslaver());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Advance to opponent's turn
        advanceTurn();

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
        enableAutoStop();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Mindslaver());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Advance to player2's turn
        advanceTurn();

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.mindControlledPlayerId).isEqualTo(player2.getId());

        // Set player2's hand AFTER turn advancement (draw step adds cards)
        Card testCard = new com.github.laxika.magicalvibes.cards.m.Memnite();
        harness.setHand(player2, List.of(testCard));

        // Controller (player1) casts from controlled player's hand
        // Memnite costs {0}, so no mana needed
        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        // The creature should be on player2's battlefield (controlled player's resources)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Memnite"));
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    // ===== Turn control deactivation =====

    @Test
    @DisplayName("Mind control ends when controlled turn ends")
    void turnControlEndsAtTurnEnd() {
        enableAutoStop();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Mindslaver());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Advance to player2's turn (mind control activates)
        advanceTurn();
        assertThat(gd.mindControlledPlayerId).isEqualTo(player2.getId());

        // Advance past the controlled turn to player1's turn
        advanceTurn();

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
