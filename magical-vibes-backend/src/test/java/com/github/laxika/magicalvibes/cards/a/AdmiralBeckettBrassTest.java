package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainControlIfSubtypesDealtCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class AdmiralBeckettBrassTest extends BaseCardTest {

    // ===== Helper: create a simple Pirate card =====

    private Card createPirateCard(String name) {
        Card card = new Card() {};
        card.setName(name);
        card.setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.PIRATE));
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    /**
     * Simulates a creature having dealt combat damage to a player this turn.
     * Populates the same tracking fields that CombatDamageService populates.
     */
    private void recordCombatDamageToPlayer(Permanent creature, UUID damagedPlayerId) {
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(creature.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(damagedPlayerId);
        if (!gd.combatDamageSourceSubtypesThisTurn.containsKey(creature.getId())) {
            Set<CardSubtype> subtypes = ConcurrentHashMap.newKeySet();
            subtypes.addAll(creature.getCard().getSubtypes());
            subtypes.addAll(creature.getGrantedSubtypes());
            subtypes.addAll(creature.getTransientSubtypes());
            gd.combatDamageSourceSubtypesThisTurn.put(creature.getId(), subtypes);
        }
    }

    // ===== Card properties =====

    @Nested
    @DisplayName("Card properties")
    class CardProperties {

        @Test
        @DisplayName("Has static lord effect for other Pirates")
        void hasStaticLordEffect() {
            AdmiralBeckettBrass card = new AdmiralBeckettBrass();

            assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
            StaticBoostEffect boost = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
            assertThat(boost.powerBoost()).isEqualTo(1);
            assertThat(boost.toughnessBoost()).isEqualTo(1);
            assertThat(boost.scope()).isEqualTo(GrantScope.OWN_CREATURES);
            assertThat(boost.filter()).isInstanceOf(PermanentHasAnySubtypePredicate.class);
        }

        @Test
        @DisplayName("Has controller end-step triggered effect")
        void hasEndStepTrigger() {
            AdmiralBeckettBrass card = new AdmiralBeckettBrass();

            assertThat(card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED)).hasSize(1);
            GainControlIfSubtypesDealtCombatDamageEffect effect =
                    (GainControlIfSubtypesDealtCombatDamageEffect) card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst();
            assertThat(effect.subtype()).isEqualTo(CardSubtype.PIRATE);
            assertThat(effect.threshold()).isEqualTo(3);
        }
    }

    // ===== Lord effect =====

    @Nested
    @DisplayName("Lord effect — other Pirates get +1/+1")
    class LordEffect {

        @Test
        @DisplayName("Boosts other Pirates you control")
        void boostsOtherPirates() {
            harness.addToBattlefield(player1, new AdmiralBeckettBrass());
            Permanent pirate = harness.addToBattlefieldAndReturn(player1, createPirateCard("Pirate Deckhand"));

            assertThat(gqs.getEffectivePower(gd, pirate)).isEqualTo(3);   // 2 base + 1 lord
            assertThat(gqs.getEffectiveToughness(gd, pirate)).isEqualTo(3); // 2 base + 1 lord
        }

        @Test
        @DisplayName("Does not boost non-Pirate creatures")
        void doesNotBoostNonPirates() {
            harness.addToBattlefield(player1, new AdmiralBeckettBrass());
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

            assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        }

        @Test
        @DisplayName("Does not boost itself")
        void doesNotBoostSelf() {
            Permanent admiral = harness.addToBattlefieldAndReturn(player1, new AdmiralBeckettBrass());

            // Admiral is a 3/3 — should not get its own lord bonus
            assertThat(gqs.getEffectivePower(gd, admiral)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, admiral)).isEqualTo(3);
        }

        @Test
        @DisplayName("Does not boost opponent's Pirates")
        void doesNotBoostOpponentPirates() {
            harness.addToBattlefield(player1, new AdmiralBeckettBrass());
            Permanent opponentPirate = harness.addToBattlefieldAndReturn(player2, createPirateCard("Opponent Pirate"));

            assertThat(gqs.getEffectivePower(gd, opponentPirate)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, opponentPirate)).isEqualTo(2);
        }
    }

    // ===== End-step trigger =====

    @Nested
    @DisplayName("End-step trigger — gain control if 3+ Pirates dealt combat damage")
    class EndStepTrigger {

        @Test
        @DisplayName("Trigger fires when 3 Pirates dealt combat damage to an opponent")
        void triggerFiresWithThreePirates() {
            harness.addToBattlefield(player1, new AdmiralBeckettBrass());
            Permanent pirate1 = harness.addToBattlefieldAndReturn(player1, createPirateCard("Pirate A"));
            Permanent pirate2 = harness.addToBattlefieldAndReturn(player1, createPirateCard("Pirate B"));
            Permanent pirate3 = harness.addToBattlefieldAndReturn(player1, createPirateCard("Pirate C"));

            // Opponent has a nonland permanent to steal
            Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

            // Simulate 3 Pirates dealing combat damage to player2
            recordCombatDamageToPlayer(pirate1, player2.getId());
            recordCombatDamageToPlayer(pirate2, player2.getId());
            recordCombatDamageToPlayer(pirate3, player2.getId());

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
            harness.clearPriorityPassed();

            // Advance to end step → triggers Admiral's ability
            harness.passBothPriorities();

            assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

            // Choose the opponent's Grizzly Bears
            harness.handlePermanentChosen(player1, opponentBears.getId());

            // Resolve the triggered ability
            harness.passBothPriorities();

            // Grizzly Bears should now be on player1's battlefield
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Trigger does not fire when only 2 Pirates dealt combat damage")
        void triggerDoesNotFireWithTwoPirates() {
            harness.setHand(player1, List.of());
            harness.setHand(player2, List.of());

            harness.addToBattlefield(player1, new AdmiralBeckettBrass());
            Permanent pirate1 = harness.addToBattlefieldAndReturn(player1, createPirateCard("Pirate A"));
            Permanent pirate2 = harness.addToBattlefieldAndReturn(player1, createPirateCard("Pirate B"));

            harness.addToBattlefield(player2, new GrizzlyBears());

            // Only 2 Pirates dealt combat damage
            recordCombatDamageToPlayer(pirate1, player2.getId());
            recordCombatDamageToPlayer(pirate2, player2.getId());

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
            harness.clearPriorityPassed();

            // Advance to end step — trigger should NOT fire (only 2 Pirates, need 3)
            harness.passBothPriorities();

            // Opponent's Grizzly Bears should still be on their battlefield (not stolen)
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Trigger does not fire when no combat damage was dealt")
        void triggerDoesNotFireWithNoCombatDamage() {
            harness.setHand(player1, List.of());
            harness.setHand(player2, List.of());

            harness.addToBattlefield(player1, new AdmiralBeckettBrass());
            harness.addToBattlefield(player2, new GrizzlyBears());

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.passBothPriorities();

            // Opponent's Grizzly Bears should still be on their battlefield (not stolen)
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Non-Pirate creatures dealing combat damage don't count toward threshold")
        void nonPiratesDontCount() {
            harness.setHand(player1, List.of());
            harness.setHand(player2, List.of());

            harness.addToBattlefield(player1, new AdmiralBeckettBrass());
            Permanent pirate1 = harness.addToBattlefieldAndReturn(player1, createPirateCard("Pirate A"));
            Permanent pirate2 = harness.addToBattlefieldAndReturn(player1, createPirateCard("Pirate B"));
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

            harness.addToBattlefield(player2, new GrizzlyBears());

            // 2 Pirates + 1 non-Pirate dealt combat damage = only 2 Pirates count
            recordCombatDamageToPlayer(pirate1, player2.getId());
            recordCombatDamageToPlayer(pirate2, player2.getId());
            recordCombatDamageToPlayer(bears, player2.getId());

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.passBothPriorities();

            // Opponent's Grizzly Bears should still be on their battlefield (not stolen)
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Trigger skips if opponent has no nonland permanents")
        void triggerSkipsIfNoValidTargets() {
            harness.setHand(player1, List.of());
            harness.setHand(player2, List.of());

            harness.addToBattlefield(player1, new AdmiralBeckettBrass());
            Permanent pirate1 = harness.addToBattlefieldAndReturn(player1, createPirateCard("Pirate A"));
            Permanent pirate2 = harness.addToBattlefieldAndReturn(player1, createPirateCard("Pirate B"));
            Permanent pirate3 = harness.addToBattlefieldAndReturn(player1, createPirateCard("Pirate C"));

            // Opponent has NO nonland permanents (empty battlefield)
            recordCombatDamageToPlayer(pirate1, player2.getId());
            recordCombatDamageToPlayer(pirate2, player2.getId());
            recordCombatDamageToPlayer(pirate3, player2.getId());

            int player1BattlefieldSize = gd.playerBattlefields.get(player1.getId()).size();

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.passBothPriorities();

            // The trigger condition is met (3 Pirates), but there are no valid targets (opponent has no nonland permanents).
            // Player1's battlefield should not have gained any permanents from player2.
            assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
            assertThat(gd.playerBattlefields.get(player1.getId()).size()).isEqualTo(player1BattlefieldSize);
        }

        @Test
        @DisplayName("Lands are not valid targets for the trigger")
        void landsAreNotValidTargets() {
            harness.addToBattlefield(player1, new AdmiralBeckettBrass());
            Permanent pirate1 = harness.addToBattlefieldAndReturn(player1, createPirateCard("Pirate A"));
            Permanent pirate2 = harness.addToBattlefieldAndReturn(player1, createPirateCard("Pirate B"));
            Permanent pirate3 = harness.addToBattlefieldAndReturn(player1, createPirateCard("Pirate C"));

            // Opponent has a nonland permanent and a land
            Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

            Card landCard = new Card() {};
            landCard.setName("Island");
            landCard.setType(CardType.LAND);
            landCard.setSubtypes(List.of(CardSubtype.ISLAND));
            Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, landCard);

            recordCombatDamageToPlayer(pirate1, player2.getId());
            recordCombatDamageToPlayer(pirate2, player2.getId());
            recordCombatDamageToPlayer(pirate3, player2.getId());

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

            // Land should not be a valid target
            assertThat(gd.interaction.permanentChoice().validIds()).doesNotContain(opponentLand.getId());
            // Grizzly Bears should be a valid target
            assertThat(gd.interaction.permanentChoice().validIds()).contains(opponentBears.getId());
        }

        @Test
        @DisplayName("Own permanents are not valid targets for the trigger")
        void ownPermanentsAreNotValidTargets() {
            harness.addToBattlefield(player1, new AdmiralBeckettBrass());
            Permanent pirate1 = harness.addToBattlefieldAndReturn(player1, createPirateCard("Pirate A"));
            Permanent pirate2 = harness.addToBattlefieldAndReturn(player1, createPirateCard("Pirate B"));
            Permanent pirate3 = harness.addToBattlefieldAndReturn(player1, createPirateCard("Pirate C"));

            Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

            recordCombatDamageToPlayer(pirate1, player2.getId());
            recordCombatDamageToPlayer(pirate2, player2.getId());
            recordCombatDamageToPlayer(pirate3, player2.getId());

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

            // Own Pirates should not be valid targets
            assertThat(gd.interaction.permanentChoice().validIds()).doesNotContain(pirate1.getId());
            assertThat(gd.interaction.permanentChoice().validIds()).doesNotContain(pirate2.getId());
            assertThat(gd.interaction.permanentChoice().validIds()).doesNotContain(pirate3.getId());
            // Opponent's creature should be a valid target
            assertThat(gd.interaction.permanentChoice().validIds()).contains(opponentBears.getId());
        }

        @Test
        @DisplayName("Does not trigger on opponent's end step")
        void doesNotTriggerOnOpponentEndStep() {
            harness.addToBattlefield(player1, new AdmiralBeckettBrass());
            Permanent pirate1 = harness.addToBattlefieldAndReturn(player1, createPirateCard("Pirate A"));
            Permanent pirate2 = harness.addToBattlefieldAndReturn(player1, createPirateCard("Pirate B"));
            Permanent pirate3 = harness.addToBattlefieldAndReturn(player1, createPirateCard("Pirate C"));

            harness.addToBattlefield(player2, new GrizzlyBears());

            recordCombatDamageToPlayer(pirate1, player2.getId());
            recordCombatDamageToPlayer(pirate2, player2.getId());
            recordCombatDamageToPlayer(pirate3, player2.getId());

            // It's player2's turn, not player1's
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

            gs.advanceStep(gd);

            assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
            // Admiral Beckett Brass belongs to player1 — should NOT trigger on player2's end step
            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
        }
    }
}
