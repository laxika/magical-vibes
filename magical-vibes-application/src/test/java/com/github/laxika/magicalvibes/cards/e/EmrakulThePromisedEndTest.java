package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.NebelgastHerald;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WildFieldScarecrow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmrakulThePromisedEnd.class, GrizzlyBears.class, Shock.class, Mountain.class,
        Cancel.class, WildFieldScarecrow.class, NebelgastHerald.class})
class EmrakulThePromisedEndTest extends BaseCardTest {

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
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
    @CardUsed({EmrakulThePromisedEnd.class, GrizzlyBears.class, Shock.class, Mountain.class,
            WildFieldScarecrow.class})
    @DisplayName("Cost reduction")
    class CostReduction {

        @Test
        void artifactCreatureContributesTwoTypes() {
            harness.setGraveyard(player1, List.of(new WildFieldScarecrow()));
            harness.setHand(player1, List.of(new EmrakulThePromisedEnd()));
            harness.addMana(player1, ManaColor.COLORLESS, 11);

            harness.castCreature(player1, 0);
            harness.handlePermanentChosen(player1, player2.getId());

            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        }

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
    @CardUsed({EmrakulThePromisedEnd.class, Cancel.class})
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
            harness.assertOnBattlefield(player1, "Emrakul, the Promised End");
        }

        @Test
        @DisplayName("Opponent's next turn is controlled and queues their extra turn")
        void controlledTurnThenExtraTurn() {
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

        private void castAndCounterEmrakul() {
            EmrakulThePromisedEnd emrakul = new EmrakulThePromisedEnd();
            harness.setHand(player1, List.of(emrakul));
            harness.addMana(player1, ManaColor.COLORLESS, 13);
            harness.setHand(player2, List.of(new Cancel()));
            harness.addMana(player2, ManaColor.BLUE, 3);
            harness.castCreature(player1, 0);
            harness.handlePermanentChosen(player1, player2.getId());
            harness.passBothPriorities();
            harness.passPriority(player1);
            harness.castAndResolveInstant(player2, 0, emrakul.getId());
            harness.assertInGraveyard(player1, "Emrakul, the Promised End");
            harness.assertNotOnBattlefield(player1, "Emrakul, the Promised End");
        }

        @Test
        void counteringCreatureDoesNotUndoResolvedCastTrigger() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            castAndCounterEmrakul();

            advanceTurn();
            assertThat(gd.activePlayerId).isEqualTo(player2.getId());
            assertThat(gd.mindControllerPlayerId).isEqualTo(player1.getId());
            advanceTurn();
            assertThat(gd.activePlayerId).isEqualTo(player2.getId());
            assertThat(gd.mindControllerPlayerId).isNull();
        }

        @Test
        void twoResolvedCastTriggersGiveTwoUncontrolledExtraTurns() {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            castAndCounterEmrakul();
            castAndCounterEmrakul();

            advanceTurn();
            assertThat(gd.activePlayerId).isEqualTo(player2.getId());
            assertThat(gd.mindControllerPlayerId).isEqualTo(player1.getId());
            advanceTurn();
            assertThat(gd.activePlayerId).isEqualTo(player2.getId());
            assertThat(gd.mindControllerPlayerId).isNull();
            advanceTurn();
            assertThat(gd.activePlayerId).isEqualTo(player2.getId());
            assertThat(gd.mindControllerPlayerId).isNull();
            advanceTurn();
            assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        }
    }

    @Nested
    @CardUsed({EmrakulThePromisedEnd.class, Shock.class})
    class Protection {

        @Test
        void enteringWithoutCastingDoesNotControlOpponent() {
            harness.enterBattlefieldAndReturn(player1, new EmrakulThePromisedEnd());

            assertThat(gd.stack).isEmpty();
            advanceTurn();
            assertThat(gd.mindControllerPlayerId).isNull();
            assertThat(gd.extraTurns).isEmpty();
        }

        @Test
        void instantCannotTargetEmrakulOnBattlefield() {
            var emrakul = harness.addToBattlefieldAndReturn(player2, new EmrakulThePromisedEnd());
            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, emrakul.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @CardUsed({EmrakulThePromisedEnd.class, GrizzlyBears.class, NebelgastHerald.class})
    class Combat {

        @Test
        void creatureWithoutFlyingOrReachCannotBlockEmrakul() {
            addCreatureReady(player1, new EmrakulThePromisedEnd());
            addCreatureReady(player2, new GrizzlyBears());
            declareAttackersAndPrepareBlockers(List.of(0));

            assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                    List.of(new BlockerAssignment(0, 0))))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void trampleDealsExcessDamageThroughFlyingBlocker() {
            var emrakul = addCreatureReady(player1, new EmrakulThePromisedEnd());
            var blocker = addCreatureReady(player2, new NebelgastHerald());
            harness.setLife(player2, 20);
            declareAttackersAndPrepareBlockers(List.of(0));
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
            harness.handleCombatDamageAssigned(player1, 0,
                    Map.of(blocker.getId(), 1, player2.getId(), 12));

            harness.assertLife(player2, 8);
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(emrakul);
            assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        }
    }
}
