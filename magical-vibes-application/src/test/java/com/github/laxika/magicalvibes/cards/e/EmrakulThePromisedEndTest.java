package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmrakulThePromisedEndTest extends BaseCardTest {

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

    private void castEmrakulTargetingOpponent() {
        harness.setHand(player1, List.of(new EmrakulThePromisedEnd()));
        harness.addMana(player1, ManaColor.COLORLESS, 13);
        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve cast trigger
        harness.passBothPriorities(); // resolve creature spell
    }

    @Nested
    @DisplayName("Cost reduction")
    class CostReduction {

        @Test
        @DisplayName("Can cast for full {13} with empty graveyard")
        void canCastForFullCost() {
            harness.setHand(player1, List.of(new EmrakulThePromisedEnd()));
            harness.addMana(player1, ManaColor.COLORLESS, 13);

            harness.castCreature(player1, 0);
            harness.handlePermanentChosen(player1, player2.getId());

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Distinct card types in graveyard each reduce the cost by {1}")
        void costReducedByDistinctCardTypes() {
            // Creature + Instant + Land = 3 types → {10}
            harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock(), new Mountain()));
            harness.setHand(player1, List.of(new EmrakulThePromisedEnd()));
            harness.addMana(player1, ManaColor.COLORLESS, 10);

            harness.castCreature(player1, 0);
            harness.handlePermanentChosen(player1, player2.getId());

            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Duplicate types among graveyard cards count only once")
        void duplicateTypesCountOnce() {
            // Two creatures + one instant = 2 types → {11}
            harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Shock()));
            harness.setHand(player1, List.of(new EmrakulThePromisedEnd()));
            harness.addMana(player1, ManaColor.COLORLESS, 11);

            harness.castCreature(player1, 0);
            harness.handlePermanentChosen(player1, player2.getId());

            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Opponent graveyard types do not reduce the cost")
        void opponentGraveyardDoesNotReduceCost() {
            harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Shock(), new Mountain()));
            harness.setHand(player1, List.of(new EmrakulThePromisedEnd()));
            harness.addMana(player1, ManaColor.COLORLESS, 12);

            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Nested
    @DisplayName("Cast trigger — control opponent + extra turn")
    class CastTrigger {

        @Test
        @DisplayName("Resolving the cast trigger sets pending turn control with extra turn")
        void setsPendingControlAndExtraTurnFlag() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            castEmrakulTargetingOpponent();

            UUID opponentId = player2.getId();
            assertThat(gd.pendingTurnControl).containsEntry(opponentId, player1.getId());
            assertThat(gd.pendingTurnControlExtraTurn).contains(opponentId);
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Emrakul, the Promised End"));
        }

        @Test
        @DisplayName("Opponent's next turn is controlled and queues their extra turn")
        void controlledTurnThenExtraTurn() {
            enableAutoStop();
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            castEmrakulTargetingOpponent();

            UUID opponentId = player2.getId();
            advanceTurn();

            assertThat(gd.activePlayerId).isEqualTo(opponentId);
            assertThat(gd.mindControlledPlayerId).isEqualTo(opponentId);
            assertThat(gd.mindControllerPlayerId).isEqualTo(player1.getId());
            assertThat(gd.extraTurns).containsExactly(opponentId);
            assertThat(gd.pendingTurnControl).isEmpty();
            assertThat(gd.pendingTurnControlExtraTurn).isEmpty();

            advanceTurn();

            // Extra turn for the opponent — no longer controlled
            assertThat(gd.activePlayerId).isEqualTo(opponentId);
            assertThat(gd.mindControlledPlayerId).isNull();
            assertThat(gd.mindControllerPlayerId).isNull();
        }

        @Test
        @DisplayName("Cast trigger cannot target the controller")
        void cannotTargetSelf() {
            harness.setHand(player1, List.of(new EmrakulThePromisedEnd()));
            harness.addMana(player1, ManaColor.COLORLESS, 13);
            harness.castCreature(player1, 0);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
