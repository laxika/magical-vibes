package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MillHalfLibraryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FleetSwallowerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Fleet Swallower has ON_ATTACK MillHalfLibraryEffect with roundUp=true")
    void hasCorrectEffects() {
        FleetSwallower card = new FleetSwallower();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst()).isInstanceOf(MillHalfLibraryEffect.class);
        MillHalfLibraryEffect effect = (MillHalfLibraryEffect) card.getEffects(EffectSlot.ON_ATTACK).getFirst();
        assertThat(effect.roundUp()).isTrue();
    }

    // ===== Attack trigger =====

    @Nested
    @DisplayName("Attack trigger")
    class AttackTrigger {

        @Test
        @DisplayName("Attacking with Fleet Swallower queues attack trigger for player target selection")
        void attackTriggerQueuesForTargetSelection() {
            addReadyFleetSwallower(player1);

            declareAttackers(List.of(0));

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
            assertThat(gd.interaction.permanentChoiceContext())
                    .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        }

        @Test
        @DisplayName("Attack trigger valid targets contain only player IDs, not permanents")
        void attackTriggerTargetsOnlyPlayers() {
            addReadyFleetSwallower(player1);
            // Add an opponent creature — it should NOT appear as a valid target
            Permanent opponentCreature = new Permanent(new FleetSwallower());
            gd.playerBattlefields.get(player2.getId()).add(opponentCreature);

            declareAttackers(List.of(0));

            assertThat(gd.interaction.permanentChoice().validIds())
                    .containsExactlyInAnyOrder(player1.getId(), player2.getId())
                    .doesNotContain(opponentCreature.getId());
        }

        @Test
        @DisplayName("Attack trigger resolves correctly when opponent has creatures on the battlefield")
        void attackTriggerResolvesWithOpponentCreatures() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);
            addReadyFleetSwallower(player1);
            // Add an opponent creature to ensure it doesn't interfere
            Permanent opponentCreature = new Permanent(new FleetSwallower());
            gd.playerBattlefields.get(player2.getId()).add(opponentCreature);

            List<Card> deck = gd.playerDecks.get(player2.getId());
            while (deck.size() > 10) {
                deck.removeFirst();
            }

            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, player2.getId());
            harness.passBothPriorities(); // resolve attack trigger

            // 10 / 2 = 5 milled, 5 remain
            assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
            assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
        }

        @Test
        @DisplayName("Mills half of target player's library rounded up (even count)")
        void millsHalfLibraryRoundedUpEvenCount() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);
            addReadyFleetSwallower(player1);

            List<Card> deck = gd.playerDecks.get(player2.getId());
            while (deck.size() > 20) {
                deck.removeFirst();
            }

            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, player2.getId());
            harness.passBothPriorities(); // resolve attack trigger

            // 20 / 2 = 10 milled, 10 remain
            assertThat(gd.playerDecks.get(player2.getId())).hasSize(10);
            assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(10);
        }

        @Test
        @DisplayName("Mills half of target player's library rounded up (odd count)")
        void millsHalfLibraryRoundedUpOddCount() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);
            addReadyFleetSwallower(player1);

            List<Card> deck = gd.playerDecks.get(player2.getId());
            while (deck.size() > 11) {
                deck.removeFirst();
            }

            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, player2.getId());
            harness.passBothPriorities(); // resolve attack trigger

            // (11 + 1) / 2 = 6 milled (rounded up), 5 remain
            assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
            assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
        }

        @Test
        @DisplayName("Can target yourself")
        void canTargetSelf() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);
            addReadyFleetSwallower(player1);

            List<Card> deck = gd.playerDecks.get(player1.getId());
            while (deck.size() > 10) {
                deck.removeFirst();
            }

            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, player1.getId());
            harness.passBothPriorities(); // resolve attack trigger

            // 10 / 2 = 5 milled, 5 remain
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        }

        @Test
        @DisplayName("Mills 1 card when library has only 1 card (rounded up)")
        void millsOneCardWithOneCardLibrary() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);
            addReadyFleetSwallower(player1);

            List<Card> deck = gd.playerDecks.get(player2.getId());
            while (deck.size() > 1) {
                deck.removeFirst();
            }

            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, player2.getId());
            harness.passBothPriorities(); // resolve attack trigger

            // (1 + 1) / 2 = 1 milled (rounded up), 0 remain
            assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        }

        @Test
        @DisplayName("Mills nothing when library is empty")
        void millsNothingWithEmptyLibrary() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);
            addReadyFleetSwallower(player1);

            gd.playerDecks.get(player2.getId()).clear();

            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, player2.getId());
            harness.passBothPriorities(); // resolve attack trigger

            assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        }
    }

    // ===== Helpers =====

    private Permanent addReadyFleetSwallower(Player player) {
        Permanent perm = new Permanent(new FleetSwallower());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player1, attackerIndices);
    }
}
