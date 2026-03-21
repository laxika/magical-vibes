package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.ControlTargetPlayerNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EndTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExtraTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TurnResolutionServiceTest {

    @Mock private CombatService combatService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private AuraAttachmentService auraAttachmentService;
    @Mock private TurnCleanupService turnCleanupService;
    @Mock private ExileService exileService;

    @InjectMocks
    private TurnResolutionService turnResolutionService;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
    }

    // ===== Helper methods =====

    private Card createCard(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }

    private StackEntry createTargetedEntry(Card card, UUID controllerId, UUID targetId, List<com.github.laxika.magicalvibes.model.effect.CardEffect> effects) {
        return new StackEntry(
                StackEntryType.SORCERY_SPELL, card, controllerId, card.getName(),
                effects, 0, targetId, null
        );
    }

    private StackEntry createUntargetedEntry(Card card, UUID controllerId, List<com.github.laxika.magicalvibes.model.effect.CardEffect> effects) {
        return new StackEntry(
                StackEntryType.INSTANT_SPELL, card, controllerId, card.getName(),
                effects, 0
        );
    }

    // =========================================================================
    // ExtraTurnEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveExtraTurn")
    class ResolveExtraTurn {

        @Test
        @DisplayName("Grants one extra turn to the target player")
        void grantsOneExtraTurn() {
            Card card = createCard("Time Walk", CardType.SORCERY);
            ExtraTurnEffect effect = new ExtraTurnEffect(1);
            StackEntry entry = createTargetedEntry(card, player1Id, player1Id, List.of(effect));

            turnResolutionService.resolveExtraTurn(gd, entry, effect);

            assertThat(gd.extraTurns).containsExactly(player1Id);
        }

        @Test
        @DisplayName("Grants multiple extra turns to the target player")
        void grantsMultipleExtraTurns() {
            Card card = createCard("Time Stretch", CardType.SORCERY);
            ExtraTurnEffect effect = new ExtraTurnEffect(2);
            StackEntry entry = createTargetedEntry(card, player1Id, player2Id, List.of(effect));

            turnResolutionService.resolveExtraTurn(gd, entry, effect);

            assertThat(gd.extraTurns).hasSize(2);
            assertThat(gd.extraTurns).containsOnly(player2Id);
        }

        @Test
        @DisplayName("Extra turns are added to the front of the queue")
        void extraTurnsAddedToFront() {
            gd.extraTurns.addLast(player2Id);

            Card card = createCard("Time Walk", CardType.SORCERY);
            ExtraTurnEffect effect = new ExtraTurnEffect(1);
            StackEntry entry = createTargetedEntry(card, player1Id, player1Id, List.of(effect));

            turnResolutionService.resolveExtraTurn(gd, entry, effect);

            assertThat(gd.extraTurns).containsExactly(player1Id, player2Id);
        }

        @Test
        @DisplayName("Does nothing when target player ID is null")
        void doesNothingWhenTargetNull() {
            Card card = createCard("Time Walk", CardType.SORCERY);
            ExtraTurnEffect effect = new ExtraTurnEffect(1);
            StackEntry entry = createTargetedEntry(card, player1Id, null, List.of(effect));

            turnResolutionService.resolveExtraTurn(gd, entry, effect);

            assertThat(gd.extraTurns).isEmpty();
            verify(gameBroadcastService, never()).logAndBroadcast(eq(gd), org.mockito.ArgumentMatchers.anyString());
        }

        @Test
        @DisplayName("Does nothing when target player ID is not in the game")
        void doesNothingWhenTargetNotInGame() {
            UUID unknownPlayerId = UUID.randomUUID();
            Card card = createCard("Time Walk", CardType.SORCERY);
            ExtraTurnEffect effect = new ExtraTurnEffect(1);
            StackEntry entry = createTargetedEntry(card, player1Id, unknownPlayerId, List.of(effect));

            turnResolutionService.resolveExtraTurn(gd, entry, effect);

            assertThat(gd.extraTurns).isEmpty();
        }

        @Test
        @DisplayName("Logs the extra turn grant")
        void logsExtraTurnGrant() {
            Card card = createCard("Time Walk", CardType.SORCERY);
            ExtraTurnEffect effect = new ExtraTurnEffect(1);
            StackEntry entry = createTargetedEntry(card, player1Id, player1Id, List.of(effect));

            turnResolutionService.resolveExtraTurn(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), eq("Player1 takes 1 extra turn after this one."));
        }

        @Test
        @DisplayName("Log message uses plural for multiple extra turns")
        void logMessagePlural() {
            Card card = createCard("Time Stretch", CardType.SORCERY);
            ExtraTurnEffect effect = new ExtraTurnEffect(2);
            StackEntry entry = createTargetedEntry(card, player1Id, player2Id, List.of(effect));

            turnResolutionService.resolveExtraTurn(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), eq("Player2 takes 2 extra turns after this one."));
        }
    }

    // =========================================================================
    // EndTurnEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveEndTurn")
    class ResolveEndTurn {

        @Test
        @DisplayName("Clears pending may abilities (rule 723.1a)")
        void clearsPendingMayAbilities() {
            Card sourceCard = createCard("Soul Warden", CardType.CREATURE);
            gd.pendingMayAbilities.add(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                    sourceCard, player1Id, List.of(), "test"));

            Card card = createCard("Time Stop", CardType.INSTANT);
            StackEntry entry = createUntargetedEntry(card, player1Id, List.of(new EndTurnEffect()));

            turnResolutionService.resolveEndTurn(gd);

            assertThat(gd.pendingMayAbilities).isEmpty();
        }

        @Test
        @DisplayName("Exiles spell cards on the stack (rule 723.1b)")
        void exilesSpellsOnStack() {
            Card creatureCard = createCard("Grizzly Bears", CardType.CREATURE);
            Card sorceryCard = createCard("Divination", CardType.SORCERY);
            StackEntry creatureEntry = new StackEntry(creatureCard, player1Id);
            StackEntry sorceryEntry = new StackEntry(
                    StackEntryType.SORCERY_SPELL, sorceryCard, player2Id, "Divination",
                    List.of(), 0
            );
            gd.stack.add(creatureEntry);
            gd.stack.add(sorceryEntry);

            Card timeStop = createCard("Time Stop", CardType.INSTANT);
            StackEntry entry = createUntargetedEntry(timeStop, player1Id, List.of(new EndTurnEffect()));

            turnResolutionService.resolveEndTurn(gd);

            assertThat(gd.stack).isEmpty();
            verify(exileService).exileCard(gd, player1Id, creatureCard);
            verify(exileService).exileCard(gd, player2Id, sorceryCard);
        }

        @Test
        @DisplayName("Triggered abilities on the stack cease to exist without exile")
        void triggeredAbilitiesCeaseToExist() {
            Card triggerSource = createCard("Soul Warden", CardType.CREATURE);
            StackEntry triggeredEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, triggerSource, player1Id, "Soul Warden trigger",
                    List.of(), 0
            );
            gd.stack.add(triggeredEntry);

            Card timeStop = createCard("Time Stop", CardType.INSTANT);
            StackEntry entry = createUntargetedEntry(timeStop, player1Id, List.of(new EndTurnEffect()));

            turnResolutionService.resolveEndTurn(gd);

            assertThat(gd.stack).isEmpty();
            verify(exileService, never()).exileCard(eq(gd), eq(player1Id), eq(triggerSource));
        }

        @Test
        @DisplayName("Clears combat state")
        void clearsCombatState() {
            Card card = createCard("Time Stop", CardType.INSTANT);
            StackEntry entry = createUntargetedEntry(card, player1Id, List.of(new EndTurnEffect()));

            turnResolutionService.resolveEndTurn(gd);

            verify(combatService).clearCombatState(gd);
        }

        @Test
        @DisplayName("Clears sacrifice-at-end-of-combat and token-exile-at-end-of-combat sets")
        void clearsCombatEndSets() {
            gd.permanentsToSacrificeAtEndOfCombat.add(UUID.randomUUID());
            gd.pendingTokenExilesAtEndOfCombat.add(UUID.randomUUID());

            Card card = createCard("Time Stop", CardType.INSTANT);
            StackEntry entry = createUntargetedEntry(card, player1Id, List.of(new EndTurnEffect()));

            turnResolutionService.resolveEndTurn(gd);

            assertThat(gd.permanentsToSacrificeAtEndOfCombat).isEmpty();
            assertThat(gd.pendingTokenExilesAtEndOfCombat).isEmpty();
        }

        @Test
        @DisplayName("Sets current step to CLEANUP (rule 723.1d)")
        void setsStepToCleanup() {
            gd.currentStep = TurnStep.PRECOMBAT_MAIN;

            Card card = createCard("Time Stop", CardType.INSTANT);
            StackEntry entry = createUntargetedEntry(card, player1Id, List.of(new EndTurnEffect()));

            turnResolutionService.resolveEndTurn(gd);

            assertThat(gd.currentStep).isEqualTo(TurnStep.CLEANUP);
        }

        @Test
        @DisplayName("Resets end-of-turn modifiers")
        void resetsEndOfTurnModifiers() {
            Card card = createCard("Time Stop", CardType.INSTANT);
            StackEntry entry = createUntargetedEntry(card, player1Id, List.of(new EndTurnEffect()));

            turnResolutionService.resolveEndTurn(gd);

            verify(turnCleanupService).resetEndOfTurnModifiers(gd);
        }

        @Test
        @DisplayName("Returns stolen creatures")
        void returnsStolenCreatures() {
            Card card = createCard("Time Stop", CardType.INSTANT);
            StackEntry entry = createUntargetedEntry(card, player1Id, List.of(new EndTurnEffect()));

            turnResolutionService.resolveEndTurn(gd);

            verify(auraAttachmentService).returnStolenCreatures(gd, true);
        }

        @Test
        @DisplayName("Clears priority passed set")
        void clearsPriorityPassed() {
            gd.priorityPassedBy.add(player1Id);
            gd.priorityPassedBy.add(player2Id);

            Card card = createCard("Time Stop", CardType.INSTANT);
            StackEntry entry = createUntargetedEntry(card, player1Id, List.of(new EndTurnEffect()));

            turnResolutionService.resolveEndTurn(gd);

            assertThat(gd.priorityPassedBy).isEmpty();
        }

        @Test
        @DisplayName("Sets endTurnRequested flag")
        void setsEndTurnRequestedFlag() {
            assertThat(gd.endTurnRequested).isFalse();

            Card card = createCard("Time Stop", CardType.INSTANT);
            StackEntry entry = createUntargetedEntry(card, player1Id, List.of(new EndTurnEffect()));

            turnResolutionService.resolveEndTurn(gd);

            assertThat(gd.endTurnRequested).isTrue();
        }

        @Test
        @DisplayName("Logs 'The turn ends.'")
        void logsTurnEnds() {
            Card card = createCard("Time Stop", CardType.INSTANT);
            StackEntry entry = createUntargetedEntry(card, player1Id, List.of(new EndTurnEffect()));

            turnResolutionService.resolveEndTurn(gd);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), eq("The turn ends."));
        }

        @Test
        @DisplayName("Exiles multiple spell types from the stack")
        void exilesMultipleSpellTypes() {
            Card instant = createCard("Lightning Bolt", CardType.INSTANT);
            Card enchantment = createCard("Pacifism", CardType.ENCHANTMENT);
            Card artifact = createCard("Sol Ring", CardType.ARTIFACT);

            gd.stack.add(new StackEntry(
                    StackEntryType.INSTANT_SPELL, instant, player1Id, "Lightning Bolt",
                    List.of(), 0
            ));
            gd.stack.add(new StackEntry(
                    StackEntryType.ENCHANTMENT_SPELL, enchantment, player2Id, "Pacifism",
                    List.of(), 0
            ));
            gd.stack.add(new StackEntry(
                    StackEntryType.ARTIFACT_SPELL, artifact, player1Id, "Sol Ring",
                    List.of(), 0
            ));

            Card timeStop = createCard("Time Stop", CardType.INSTANT);
            StackEntry entry = createUntargetedEntry(timeStop, player1Id, List.of(new EndTurnEffect()));

            turnResolutionService.resolveEndTurn(gd);

            assertThat(gd.stack).isEmpty();
            verify(exileService).exileCard(gd, player1Id, instant);
            verify(exileService).exileCard(gd, player2Id, enchantment);
            verify(exileService).exileCard(gd, player1Id, artifact);
        }
    }

    // =========================================================================
    // ControlTargetPlayerNextTurnEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveControlTargetPlayerNextTurn")
    class ResolveControlTargetPlayerNextTurn {

        @Test
        @DisplayName("Registers pending turn control for the target player")
        void registersTurnControl() {
            Card card = createCard("Mindslaver", CardType.ARTIFACT);
            ControlTargetPlayerNextTurnEffect effect = new ControlTargetPlayerNextTurnEffect();
            StackEntry entry = createTargetedEntry(card, player1Id, player2Id, List.of(effect));

            turnResolutionService.resolveControlTargetPlayerNextTurn(gd, entry);

            assertThat(gd.pendingTurnControl).containsEntry(player2Id, player1Id);
        }

        @Test
        @DisplayName("Logs the turn control message")
        void logsTurnControl() {
            Card card = createCard("Mindslaver", CardType.ARTIFACT);
            ControlTargetPlayerNextTurnEffect effect = new ControlTargetPlayerNextTurnEffect();
            StackEntry entry = createTargetedEntry(card, player1Id, player2Id, List.of(effect));

            turnResolutionService.resolveControlTargetPlayerNextTurn(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Player1 will control Player2 during their next turn."));
        }

        @Test
        @DisplayName("Does nothing when target player ID is null")
        void doesNothingWhenTargetNull() {
            Card card = createCard("Mindslaver", CardType.ARTIFACT);
            ControlTargetPlayerNextTurnEffect effect = new ControlTargetPlayerNextTurnEffect();
            StackEntry entry = createTargetedEntry(card, player1Id, null, List.of(effect));

            turnResolutionService.resolveControlTargetPlayerNextTurn(gd, entry);

            assertThat(gd.pendingTurnControl).isEmpty();
            verify(gameBroadcastService, never()).logAndBroadcast(eq(gd), org.mockito.ArgumentMatchers.anyString());
        }

        @Test
        @DisplayName("Does nothing when target player is not in the game")
        void doesNothingWhenTargetNotInGame() {
            UUID unknownId = UUID.randomUUID();
            Card card = createCard("Mindslaver", CardType.ARTIFACT);
            ControlTargetPlayerNextTurnEffect effect = new ControlTargetPlayerNextTurnEffect();
            StackEntry entry = createTargetedEntry(card, player1Id, unknownId, List.of(effect));

            turnResolutionService.resolveControlTargetPlayerNextTurn(gd, entry);

            assertThat(gd.pendingTurnControl).isEmpty();
        }

        @Test
        @DisplayName("Overwrites previous turn control for the same target")
        void overwritesPreviousTurnControl() {
            gd.pendingTurnControl.put(player2Id, player2Id);

            Card card = createCard("Mindslaver", CardType.ARTIFACT);
            ControlTargetPlayerNextTurnEffect effect = new ControlTargetPlayerNextTurnEffect();
            StackEntry entry = createTargetedEntry(card, player1Id, player2Id, List.of(effect));

            turnResolutionService.resolveControlTargetPlayerNextTurn(gd, entry);

            assertThat(gd.pendingTurnControl).containsEntry(player2Id, player1Id);
        }
    }

    // =========================================================================
    // AdditionalCombatMainPhaseEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveAdditionalCombatMainPhase")
    class ResolveAdditionalCombatMainPhase {

        @Test
        @DisplayName("Adds one additional combat/main phase pair")
        void addsOnePair() {
            Card card = createCard("Relentless Assault", CardType.SORCERY);
            AdditionalCombatMainPhaseEffect effect = new AdditionalCombatMainPhaseEffect(1);
            StackEntry entry = createUntargetedEntry(card, player1Id, List.of(effect));

            turnResolutionService.resolveAdditionalCombatMainPhase(gd, entry, effect);

            assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);
        }

        @Test
        @DisplayName("Adds multiple additional combat/main phase pairs")
        void addsMultiplePairs() {
            Card card = createCard("World at War", CardType.SORCERY);
            AdditionalCombatMainPhaseEffect effect = new AdditionalCombatMainPhaseEffect(2);
            StackEntry entry = createUntargetedEntry(card, player1Id, List.of(effect));

            turnResolutionService.resolveAdditionalCombatMainPhase(gd, entry, effect);

            assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(2);
        }

        @Test
        @DisplayName("Stacks with existing additional combat/main phase pairs")
        void stacksWithExistingPairs() {
            gd.additionalCombatMainPhasePairs = 1;

            Card card = createCard("Relentless Assault", CardType.SORCERY);
            AdditionalCombatMainPhaseEffect effect = new AdditionalCombatMainPhaseEffect(1);
            StackEntry entry = createUntargetedEntry(card, player1Id, List.of(effect));

            turnResolutionService.resolveAdditionalCombatMainPhase(gd, entry, effect);

            assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(2);
        }

        @Test
        @DisplayName("Does nothing when count is zero")
        void doesNothingWhenCountZero() {
            Card card = createCard("Noop", CardType.SORCERY);
            AdditionalCombatMainPhaseEffect effect = new AdditionalCombatMainPhaseEffect(0);
            StackEntry entry = createUntargetedEntry(card, player1Id, List.of(effect));

            turnResolutionService.resolveAdditionalCombatMainPhase(gd, entry, effect);

            assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(0);
            verify(gameBroadcastService, never()).logAndBroadcast(eq(gd), org.mockito.ArgumentMatchers.anyString());
        }

        @Test
        @DisplayName("Does nothing when count is negative")
        void doesNothingWhenCountNegative() {
            Card card = createCard("Noop", CardType.SORCERY);
            AdditionalCombatMainPhaseEffect effect = new AdditionalCombatMainPhaseEffect(-1);
            StackEntry entry = createUntargetedEntry(card, player1Id, List.of(effect));

            turnResolutionService.resolveAdditionalCombatMainPhase(gd, entry, effect);

            assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(0);
        }

        @Test
        @DisplayName("Log message uses singular for one pair")
        void logMessageSingular() {
            Card card = createCard("Relentless Assault", CardType.SORCERY);
            AdditionalCombatMainPhaseEffect effect = new AdditionalCombatMainPhaseEffect(1);
            StackEntry entry = createUntargetedEntry(card, player1Id, List.of(effect));

            turnResolutionService.resolveAdditionalCombatMainPhase(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("After this main phase, there is an additional combat phase followed by an additional main phase."));
        }

        @Test
        @DisplayName("Log message uses plural for multiple pairs")
        void logMessagePlural() {
            Card card = createCard("World at War", CardType.SORCERY);
            AdditionalCombatMainPhaseEffect effect = new AdditionalCombatMainPhaseEffect(3);
            StackEntry entry = createUntargetedEntry(card, player1Id, List.of(effect));

            turnResolutionService.resolveAdditionalCombatMainPhase(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("After this main phase, there are 3 additional combat phases followed by additional main phases."));
        }
    }
}
