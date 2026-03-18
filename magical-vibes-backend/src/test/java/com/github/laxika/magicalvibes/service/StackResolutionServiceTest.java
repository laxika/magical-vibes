package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingReturnToHandOnDiscardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CantHaveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedWishCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithXChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithXPlusOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CloneService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StackResolutionServiceTest {

    @Mock private BattlefieldEntryService battlefieldEntryService;
    @Mock private CloneService cloneService;
    @Mock private GraveyardService graveyardService;
    @Mock private LegendRuleService legendRuleService;
    @Mock private StateBasedActionService stateBasedActionService;
    @Mock private GameQueryService gameQueryService;
    @Mock private TargetLegalityService targetLegalityService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private EffectResolutionService effectResolutionService;
    @Mock private PlayerInputService playerInputService;
    @Mock private TriggerCollectionService triggerCollectionService;
    @Mock private CreatureControlService creatureControlService;
    @Mock private StateTriggerService stateTriggerService;
    @Mock private ExileService exileService;

    @InjectMocks
    private StackResolutionService svc;

    @Captor private ArgumentCaptor<Permanent> permanentCaptor;

    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        gd = new GameData(UUID.randomUUID(), "test-game", PLAYER1_ID, "Player1");
        gd.playerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.orderedPlayerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.playerIdToName.put(PLAYER1_ID, "Player1");
        gd.playerIdToName.put(PLAYER2_ID, "Player2");
        gd.playerBattlefields.put(PLAYER1_ID, new ArrayList<>());
        gd.playerBattlefields.put(PLAYER2_ID, new ArrayList<>());
        gd.playerGraveyards.put(PLAYER1_ID, new ArrayList<>());
        gd.playerGraveyards.put(PLAYER2_ID, new ArrayList<>());
        gd.playerExiledCards.put(PLAYER1_ID, new ArrayList<>());
        gd.playerExiledCards.put(PLAYER2_ID, new ArrayList<>());
        gd.playerDecks.put(PLAYER1_ID, new ArrayList<>());
        gd.playerDecks.put(PLAYER2_ID, new ArrayList<>());
        gd.playerHands.put(PLAYER1_ID, new ArrayList<>());
        gd.playerHands.put(PLAYER2_ID, new ArrayList<>());
    }

    private Card createCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("1G");
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private Card createEnchantment(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.setManaCost("1W");
        return card;
    }

    private Card createAura(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.setSubtypes(List.of(CardSubtype.AURA));
        card.setManaCost("1W");
        return card;
    }

    private Card createCurseAura(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.setSubtypes(List.of(CardSubtype.AURA, CardSubtype.CURSE));
        card.setManaCost("2B");
        return card;
    }

    private Card createArtifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setManaCost("2");
        return card;
    }

    private Card createPlaneswalker(String name, int loyalty) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.PLANESWALKER);
        card.setManaCost("3U");
        card.setLoyalty(loyalty);
        return card;
    }

    private Card createInstant(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("R");
        return card;
    }

    private Card createSorcery(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.SORCERY);
        card.setManaCost("1B");
        return card;
    }

    @Nested
    @DisplayName("resolveTopOfStack basics")
    class ResolveTopOfStack {

        @Test
        @DisplayName("Does nothing when the stack is empty")
        void doesNothingWhenStackEmpty() {
            gd.stack.clear();

            svc.resolveTopOfStack(gd);

            assertThat(gd.stack).isEmpty();
            verifyNoInteractions(battlefieldEntryService, graveyardService,
                    effectResolutionService, stateBasedActionService,
                    gameBroadcastService, stateTriggerService);
        }

        @Test
        @DisplayName("Cleans up state-trigger tracking when entry leaves the stack")
        void cleansUpStateTriggerTracking() {
            Card card = createCreature("Trigger Creature");
            StackEntry entry = new StackEntry(card, PLAYER1_ID);
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(stateTriggerService).cleanupResolvedStateTrigger(gd, entry);
        }

        @Test
        @DisplayName("Broadcasts game state after resolution completes")
        void broadcastsGameStateAfterResolution() {
            Card card = createCreature("Broadcast Creature");
            gd.stack.addLast(new StackEntry(card, PLAYER1_ID));

            svc.resolveTopOfStack(gd);

            verify(gameBroadcastService).broadcastGameState(gd);
        }

        @Test
        @DisplayName("Clears priorityPassedBy after resolution")
        void clearsPriorityPassedBy() {
            Card card = createCreature("Test Creature");
            gd.stack.addLast(new StackEntry(card, PLAYER1_ID));
            gd.priorityPassedBy.add(PLAYER1_ID);
            gd.priorityPassedBy.add(PLAYER2_ID);

            svc.resolveTopOfStack(gd);

            assertThat(gd.priorityPassedBy).isEmpty();
        }

        @Test
        @DisplayName("Resolves the top (last) entry from the stack")
        void resolvesTopEntry() {
            Card first = createCreature("First Creature");
            Card second = createCreature("Second Creature");
            gd.stack.addLast(new StackEntry(first, PLAYER1_ID));
            gd.stack.addLast(new StackEntry(second, PLAYER1_ID));

            svc.resolveTopOfStack(gd);

            // Second creature (top of stack) should have been passed to battlefield entry
            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(PLAYER1_ID), permanentCaptor.capture());
            assertThat(permanentCaptor.getValue().getCard().getName()).isEqualTo("Second Creature");
            // First creature should still be on the stack
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("First Creature");
        }

        @Test
        @DisplayName("Skips SBA when interaction is awaiting input after resolution")
        void skipsStateBasedActionsWhenAwaitingInput() {
            Card card = createCreature("ETB Creature");
            gd.stack.addLast(new StackEntry(card, PLAYER1_ID));
            doAnswer(inv -> {
                gd.interaction.setAwaitingInput(AwaitingInput.PERMANENT_CHOICE);
                return null;
            }).when(battlefieldEntryService).handleCreatureEnteredBattlefield(
                    any(), any(), any(), any(), anyBoolean(), anyInt());

            svc.resolveTopOfStack(gd);

            verify(stateBasedActionService, never()).performStateBasedActions(any());
            verify(legendRuleService, never()).checkLegendRule(any(), any());
        }

        @Test
        @DisplayName("Processes pending discard self triggers after SBA")
        void processesPendingDiscardSelfTriggers() {
            Card spell = createInstant("Trigger Instant");
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell,
                    PLAYER1_ID, spell.getName(), List.of());
            gd.stack.addLast(entry);
            gd.pendingDiscardSelfTriggers.add(
                    new PermanentChoiceContext.DiscardTriggerAnyTarget(createCreature("Discard Source"), PLAYER1_ID, List.of()));

            svc.resolveTopOfStack(gd);

            verify(triggerCollectionService).processNextDiscardSelfTrigger(gd);
        }

        @Test
        @DisplayName("Processes pending death trigger targets after SBA")
        void processesPendingDeathTriggerTargets() {
            Card spell = createInstant("Death Instant");
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell,
                    PLAYER1_ID, spell.getName(), List.of());
            gd.stack.addLast(entry);
            gd.pendingDeathTriggerTargets.add(
                    new PermanentChoiceContext.DeathTriggerTarget(createCreature("Dying Source"), PLAYER1_ID, List.of()));

            svc.resolveTopOfStack(gd);

            verify(triggerCollectionService).processNextDeathTriggerTarget(gd);
        }

        @Test
        @DisplayName("Processes pending may abilities after SBA")
        void processesPendingMayAbilities() {
            Card spell = createInstant("May Instant");
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, spell,
                    PLAYER1_ID, spell.getName(), List.of());
            gd.stack.addLast(entry);
            gd.pendingMayAbilities.add(
                    new PendingMayAbility(createCreature("May Source"), PLAYER1_ID, List.of(), "May ability"));

            svc.resolveTopOfStack(gd);

            verify(playerInputService).processNextMayAbility(gd);
        }
    }

    @Nested
    @DisplayName("Creature spell resolution")
    class CreatureSpellResolution {

        @Test
        @DisplayName("Creature enters the battlefield under controller's control")
        void creatureEntersBattlefield() {
            Card card = createCreature("Test Creature");
            gd.stack.addLast(new StackEntry(card, PLAYER1_ID));

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(PLAYER1_ID), permanentCaptor.capture());
            assertThat(permanentCaptor.getValue().getCard().getName()).isEqualTo("Test Creature");
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Creature enters under player 2's control when they cast it")
        void creatureEntersForCorrectPlayer() {
            Card card = createCreature("P2 Creature");
            gd.stack.addLast(new StackEntry(card, PLAYER2_ID));

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(PLAYER2_ID), permanentCaptor.capture());
            assertThat(permanentCaptor.getValue().getCard().getName()).isEqualTo("P2 Creature");
        }

        @Test
        @DisplayName("Creature enters with X +1/+1 counters")
        void creatureEntersWithXPlusOnePlusOneCounters() {
            Card card = createCreature("Hydra");
            card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithXPlusOnePlusOneCountersEffect());
            StackEntry entry = new StackEntry(StackEntryType.CREATURE_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of(), 3);
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(PLAYER1_ID), permanentCaptor.capture());
            assertThat(permanentCaptor.getValue().getPlusOnePlusOneCounters()).isEqualTo(3);
        }

        @Test
        @DisplayName("Creature enters with fixed wish counters")
        void creatureEntersWithFixedWishCounters() {
            Card card = createCreature("Wish Creature");
            card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithFixedWishCountersEffect(2));
            gd.stack.addLast(new StackEntry(card, PLAYER1_ID));

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(PLAYER1_ID), permanentCaptor.capture());
            assertThat(permanentCaptor.getValue().getWishCounters()).isEqualTo(2);
        }

        @Test
        @DisplayName("CantHaveCountersEffect prevents +1/+1 counters on creature")
        void cantHaveCountersPreventsCreaturePlusOnePlusOneCounters() {
            Card card = createCreature("No Counter Hydra");
            card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithXPlusOnePlusOneCountersEffect());
            card.addEffect(EffectSlot.STATIC, new CantHaveCountersEffect());
            StackEntry entry = new StackEntry(StackEntryType.CREATURE_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of(), 5);
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(PLAYER1_ID), permanentCaptor.capture());
            assertThat(permanentCaptor.getValue().getPlusOnePlusOneCounters()).isZero();
        }

        @Test
        @DisplayName("CantHaveCountersEffect prevents wish counters on creature")
        void cantHaveCountersPreventsCreatureWishCounters() {
            Card card = createCreature("No Wish Creature");
            card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithFixedWishCountersEffect(3));
            card.addEffect(EffectSlot.STATIC, new CantHaveCountersEffect());
            gd.stack.addLast(new StackEntry(card, PLAYER1_ID));

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(PLAYER1_ID), permanentCaptor.capture());
            assertThat(permanentCaptor.getValue().getWishCounters()).isZero();
        }

        @Test
        @DisplayName("Clone replacement effect skips normal creature resolution")
        void cloneReplacementEffectSkipsCreatureResolution() {
            Card card = createCreature("Clone");
            gd.stack.addLast(new StackEntry(card, PLAYER1_ID));
            when(cloneService.prepareCloneReplacementEffect(any(), any(), any(), any())).thenReturn(true);

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService, never()).putPermanentOntoBattlefield(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Enchantment spell resolution")
    class EnchantmentSpellResolution {

        @Test
        @DisplayName("Non-aura enchantment enters the battlefield")
        void nonAuraEntersBattlefield() {
            Card card = createEnchantment("Test Enchantment");
            StackEntry entry = new StackEntry(StackEntryType.ENCHANTMENT_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of());
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(PLAYER1_ID), permanentCaptor.capture());
            assertThat(permanentCaptor.getValue().getCard().getName()).isEqualTo("Test Enchantment");
        }

        @Test
        @DisplayName("Aura attaches to its target permanent")
        void auraAttachesToTarget() {
            Card targetCard = createCreature("Target Creature");
            Permanent targetPerm = new Permanent(targetCard);
            UUID targetId = targetPerm.getId();
            gd.playerBattlefields.get(PLAYER2_ID).add(targetPerm);
            when(gameQueryService.findPermanentById(gd, targetId)).thenReturn(targetPerm);

            Card aura = createAura("Test Aura");
            StackEntry entry = new StackEntry(StackEntryType.ENCHANTMENT_SPELL, aura,
                    PLAYER1_ID, aura.getName(), List.of(), 0, targetId, null);
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(PLAYER1_ID), permanentCaptor.capture());
            assertThat(permanentCaptor.getValue().getAttachedTo()).isEqualTo(targetId);
        }

        @Test
        @DisplayName("Aura fizzles when target is no longer on the battlefield")
        void auraFizzlesWhenTargetGone() {
            UUID removedTargetId = UUID.randomUUID();

            Card aura = createAura("Fizzle Aura");
            StackEntry entry = new StackEntry(StackEntryType.ENCHANTMENT_SPELL, aura,
                    PLAYER1_ID, aura.getName(), List.of(), 0, removedTargetId, null);
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService, never()).putPermanentOntoBattlefield(any(), any(), any());
            verify(graveyardService).addCardToGraveyard(gd, PLAYER1_ID, aura);
        }

        @Test
        @DisplayName("Curse aura enters the battlefield attached to target player")
        void curseAuraEntersAttachedToPlayer() {
            Card curse = createCurseAura("Test Curse");
            StackEntry entry = new StackEntry(StackEntryType.ENCHANTMENT_SPELL, curse,
                    PLAYER1_ID, curse.getName(), List.of(), 0, PLAYER2_ID, null);
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(PLAYER1_ID), permanentCaptor.capture());
            assertThat(permanentCaptor.getValue().getAttachedTo()).isEqualTo(PLAYER2_ID);
        }

        @Test
        @DisplayName("Curse aura fizzles when target player is no longer in the game")
        void curseAuraFizzlesWhenTargetPlayerGone() {
            UUID nonExistentPlayerId = UUID.randomUUID();
            Card curse = createCurseAura("Fizzle Curse");
            StackEntry entry = new StackEntry(StackEntryType.ENCHANTMENT_SPELL, curse,
                    PLAYER1_ID, curse.getName(), List.of(), 0, nonExistentPlayerId, null);
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService, never()).putPermanentOntoBattlefield(any(), any(), any());
            verify(graveyardService).addCardToGraveyard(gd, PLAYER1_ID, curse);
        }

        @Test
        @DisplayName("Control-changing aura steals the enchanted permanent")
        void controlChangingAuraStealsPermanent() {
            Card targetCard = createCreature("Steal Target");
            Permanent targetPerm = new Permanent(targetCard);
            UUID targetId = targetPerm.getId();
            gd.playerBattlefields.get(PLAYER2_ID).add(targetPerm);
            when(gameQueryService.findPermanentById(gd, targetId)).thenReturn(targetPerm);

            Card aura = createAura("Control Aura");
            aura.addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
            StackEntry entry = new StackEntry(StackEntryType.ENCHANTMENT_SPELL, aura,
                    PLAYER1_ID, aura.getName(), List.of(), 0, targetId, null);
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(creatureControlService).stealPermanent(gd, PLAYER1_ID, targetPerm);
        }
    }

    @Nested
    @DisplayName("Artifact spell resolution")
    class ArtifactSpellResolution {

        @Test
        @DisplayName("Artifact enters the battlefield")
        void artifactEntersBattlefield() {
            Card card = createArtifact("Test Artifact");
            StackEntry entry = new StackEntry(StackEntryType.ARTIFACT_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of());
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(PLAYER1_ID), permanentCaptor.capture());
            assertThat(permanentCaptor.getValue().getCard().getName()).isEqualTo("Test Artifact");
        }

        @Test
        @DisplayName("Artifact enters with X charge counters")
        void artifactEntersWithXChargeCounters() {
            Card card = createArtifact("X Counter Artifact");
            card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithXChargeCountersEffect());
            StackEntry entry = new StackEntry(StackEntryType.ARTIFACT_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of(), 5);
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(PLAYER1_ID), permanentCaptor.capture());
            assertThat(permanentCaptor.getValue().getChargeCounters()).isEqualTo(5);
        }

        @Test
        @DisplayName("Artifact enters with fixed charge counters")
        void artifactEntersWithFixedChargeCounters() {
            Card card = createArtifact("Fixed Counter Artifact");
            card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithFixedChargeCountersEffect(3));
            StackEntry entry = new StackEntry(StackEntryType.ARTIFACT_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of());
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(PLAYER1_ID), permanentCaptor.capture());
            assertThat(permanentCaptor.getValue().getChargeCounters()).isEqualTo(3);
        }

        @Test
        @DisplayName("Artifact enters with X +1/+1 counters")
        void artifactEntersWithXPlusOnePlusOneCounters() {
            Card card = createArtifact("Modular Artifact");
            card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithXPlusOnePlusOneCountersEffect());
            StackEntry entry = new StackEntry(StackEntryType.ARTIFACT_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of(), 4);
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(PLAYER1_ID), permanentCaptor.capture());
            assertThat(permanentCaptor.getValue().getPlusOnePlusOneCounters()).isEqualTo(4);
        }

        @Test
        @DisplayName("Artifact enters with fixed wish counters")
        void artifactEntersWithFixedWishCounters() {
            Card card = createArtifact("Wish Artifact");
            card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithFixedWishCountersEffect(3));
            StackEntry entry = new StackEntry(StackEntryType.ARTIFACT_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of());
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(PLAYER1_ID), permanentCaptor.capture());
            assertThat(permanentCaptor.getValue().getWishCounters()).isEqualTo(3);
        }

        @Test
        @DisplayName("CantHaveCountersEffect prevents X charge counters")
        void cantHaveCountersPreventsXChargeCounters() {
            Card card = createArtifact("No Counter Artifact");
            card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithXChargeCountersEffect());
            card.addEffect(EffectSlot.STATIC, new CantHaveCountersEffect());
            StackEntry entry = new StackEntry(StackEntryType.ARTIFACT_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of(), 5);
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(PLAYER1_ID), permanentCaptor.capture());
            assertThat(permanentCaptor.getValue().getChargeCounters()).isZero();
        }

        @Test
        @DisplayName("CantHaveCountersEffect prevents fixed charge counters")
        void cantHaveCountersPreventsFixedChargeCounters() {
            Card card = createArtifact("No Fixed Counter Artifact");
            card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithFixedChargeCountersEffect(4));
            card.addEffect(EffectSlot.STATIC, new CantHaveCountersEffect());
            StackEntry entry = new StackEntry(StackEntryType.ARTIFACT_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of());
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(PLAYER1_ID), permanentCaptor.capture());
            assertThat(permanentCaptor.getValue().getChargeCounters()).isZero();
        }

        @Test
        @DisplayName("CantHaveCountersEffect prevents +1/+1 counters on artifact")
        void cantHaveCountersPreventsArtifactPlusOnePlusOneCounters() {
            Card card = createArtifact("No +1 Artifact");
            card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithXPlusOnePlusOneCountersEffect());
            card.addEffect(EffectSlot.STATIC, new CantHaveCountersEffect());
            StackEntry entry = new StackEntry(StackEntryType.ARTIFACT_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of(), 5);
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(PLAYER1_ID), permanentCaptor.capture());
            assertThat(permanentCaptor.getValue().getPlusOnePlusOneCounters()).isZero();
        }

        @Test
        @DisplayName("CantHaveCountersEffect prevents wish counters on artifact")
        void cantHaveCountersPreventsArtifactWishCounters() {
            Card card = createArtifact("No Wish Artifact");
            card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithFixedWishCountersEffect(3));
            card.addEffect(EffectSlot.STATIC, new CantHaveCountersEffect());
            StackEntry entry = new StackEntry(StackEntryType.ARTIFACT_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of());
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(PLAYER1_ID), permanentCaptor.capture());
            assertThat(permanentCaptor.getValue().getWishCounters()).isZero();
        }

        @Test
        @DisplayName("Clone replacement effect skips normal artifact resolution")
        void cloneReplacementEffectSkipsArtifactResolution() {
            Card card = createArtifact("Clone Artifact");
            StackEntry entry = new StackEntry(StackEntryType.ARTIFACT_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of());
            gd.stack.addLast(entry);
            when(cloneService.prepareCloneReplacementEffect(any(), any(), any(), any())).thenReturn(true);

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService, never()).putPermanentOntoBattlefield(any(), any(), any());
        }

        @Test
        @DisplayName("ChooseCardNameOnEnterEffect defers resolution for name choice")
        void chooseCardNameOnEnterDefersResolution() {
            Card card = createArtifact("Pithing Needle");
            card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseCardNameOnEnterEffect());
            StackEntry entry = new StackEntry(StackEntryType.ARTIFACT_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of());
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(playerInputService).beginCardNameChoice(gd, PLAYER1_ID, card, List.of());
            verify(battlefieldEntryService, never()).putPermanentOntoBattlefield(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Planeswalker spell resolution")
    class PlaneswalkerSpellResolution {

        @Test
        @DisplayName("Planeswalker enters with correct loyalty counters")
        void planeswalkerEntersWithLoyalty() {
            Card card = createPlaneswalker("Test Planeswalker", 4);
            StackEntry entry = new StackEntry(StackEntryType.PLANESWALKER_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of());
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(PLAYER1_ID), permanentCaptor.capture());
            assertThat(permanentCaptor.getValue().getLoyaltyCounters()).isEqualTo(4);
        }

        @Test
        @DisplayName("Planeswalker is not summoning sick")
        void planeswalkerNotSummoningSick() {
            Card card = createPlaneswalker("Active Planeswalker", 3);
            StackEntry entry = new StackEntry(StackEntryType.PLANESWALKER_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of());
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(PLAYER1_ID), permanentCaptor.capture());
            assertThat(permanentCaptor.getValue().isSummoningSick()).isFalse();
        }

        @Test
        @DisplayName("Planeswalker with null loyalty enters with 0 loyalty counters")
        void planeswalkerNullLoyaltyDefaultsToZero() {
            Card card = createPlaneswalker("Zero PW", 0);
            card.setLoyalty(null);
            StackEntry entry = new StackEntry(StackEntryType.PLANESWALKER_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of());
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(PLAYER1_ID), permanentCaptor.capture());
            assertThat(permanentCaptor.getValue().getLoyaltyCounters()).isZero();
            // SBA would destroy the 0-loyalty planeswalker in production
            verify(stateBasedActionService).performStateBasedActions(gd);
        }
    }

    @Nested
    @DisplayName("Spell and ability resolution")
    class SpellAndAbilityResolution {

        @Test
        @DisplayName("Instant resolves and goes to graveyard")
        void instantGoesToGraveyard() {
            Card card = createInstant("Test Instant");
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of());
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(effectResolutionService).resolveEffects(gd, entry);
            verify(graveyardService).addCardToGraveyard(gd, PLAYER1_ID, card);
        }

        @Test
        @DisplayName("Sorcery resolves and goes to graveyard")
        void sorceryGoesToGraveyard() {
            Card card = createSorcery("Test Sorcery");
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of());
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(effectResolutionService).resolveEffects(gd, entry);
            verify(graveyardService).addCardToGraveyard(gd, PLAYER1_ID, card);
        }

        @Test
        @DisplayName("Targeted spell fizzles when target is gone and goes to graveyard")
        void targetedSpellFizzlesGoesToGraveyard() {
            UUID nonExistentTarget = UUID.randomUUID();
            Card card = createInstant("Fizzle Instant");
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of(), 0, nonExistentTarget, null);
            gd.stack.addLast(entry);
            when(targetLegalityService.isTargetIllegalOnResolution(gd, entry)).thenReturn(true);

            svc.resolveTopOfStack(gd);

            verify(graveyardService).addCardToGraveyard(gd, PLAYER1_ID, card);
            verify(effectResolutionService, never()).resolveEffects(any(), any());
        }

        @Test
        @DisplayName("Fizzled copy does not go to graveyard (ceases to exist)")
        void fizzledCopyCeasesToExist() {
            UUID nonExistentTarget = UUID.randomUUID();
            Card card = createInstant("Copy Fizzle");
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of(), 0, nonExistentTarget, null);
            entry.setCopy(true);
            gd.stack.addLast(entry);
            when(targetLegalityService.isTargetIllegalOnResolution(gd, entry)).thenReturn(true);

            svc.resolveTopOfStack(gd);

            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
        }

        @Test
        @DisplayName("Triggered ability that fizzles does not go to graveyard")
        void triggeredAbilityFizzleNoGraveyard() {
            UUID nonExistentTarget = UUID.randomUUID();
            Card card = createCreature("Trigger Source");
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card,
                    PLAYER1_ID, "Trigger Source's ability", List.of(), 0, nonExistentTarget, null);
            gd.stack.addLast(entry);
            when(targetLegalityService.isTargetIllegalOnResolution(gd, entry)).thenReturn(true);

            svc.resolveTopOfStack(gd);

            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
        }

        @Test
        @DisplayName("Flashback spell that fizzles is exiled instead of going to graveyard")
        void flashbackSpellFizzlesIsExiled() {
            UUID nonExistentTarget = UUID.randomUUID();
            Card card = createInstant("Flashback Fizzle");
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of(), 0, nonExistentTarget, null);
            entry.setCastWithFlashback(true);
            gd.stack.addLast(entry);
            when(targetLegalityService.isTargetIllegalOnResolution(gd, entry)).thenReturn(true);

            svc.resolveTopOfStack(gd);

            verify(exileService).exileCard(gd, PLAYER1_ID, card);
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
        }

        @Test
        @DisplayName("Flashback spell resolves and is exiled instead of going to graveyard (CR 702.33a)")
        void flashbackSpellResolvesIsExiled() {
            Card card = createSorcery("Flashback Sorcery");
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of());
            entry.setCastWithFlashback(true);
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            assertThat(gd.playerExiledCards.get(PLAYER1_ID))
                    .anyMatch(c -> c.getName().equals("Flashback Sorcery"));
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
        }

        @Test
        @DisplayName("Flashback overrides return-to-hand (CR 702.33a)")
        void flashbackOverridesReturnToHand() {
            Card card = createSorcery("Flashback Buyback");
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of());
            entry.setCastWithFlashback(true);
            entry.setReturnToHandAfterResolving(true);
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            // Flashback exile overrides return-to-hand per CR 702.33a
            assertThat(gd.playerExiledCards.get(PLAYER1_ID))
                    .anyMatch(c -> c.getName().equals("Flashback Buyback"));
            assertThat(gd.playerHands.get(PLAYER1_ID))
                    .noneMatch(c -> c.getName().equals("Flashback Buyback"));
        }

        @Test
        @DisplayName("ExileSpellEffect causes spell to be exiled instead of going to graveyard")
        void exileSpellEffectExilesSpell() {
            Card card = createInstant("Exile Instant");
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of(new ExileSpellEffect()));
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            assertThat(gd.playerExiledCards.get(PLAYER1_ID))
                    .anyMatch(c -> c.getName().equals("Exile Instant"));
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
        }

        @Test
        @DisplayName("ReturnToHandAfterResolving sends spell back to hand")
        void returnToHandAfterResolving() {
            Card card = createSorcery("Buyback Sorcery");
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of());
            entry.setReturnToHandAfterResolving(true);
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            assertThat(gd.playerHands.get(PLAYER1_ID))
                    .anyMatch(c -> c.getName().equals("Buyback Sorcery"));
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
        }

        @Test
        @DisplayName("Pending return-to-hand on discard defers spell disposition")
        void pendingReturnToHandOnDiscardDefersDisposition() {
            Card card = createSorcery("Deferred Sorcery");
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of());
            gd.stack.addLast(entry);
            gd.pendingReturnToHandOnDiscardType = new PendingReturnToHandOnDiscardType(card, PLAYER1_ID, CardType.LAND);

            svc.resolveTopOfStack(gd);

            // Spell disposition is deferred — card stays in limbo
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
            assertThat(gd.playerExiledCards.get(PLAYER1_ID))
                    .noneMatch(c -> c.getName().equals("Deferred Sorcery"));
            assertThat(gd.playerHands.get(PLAYER1_ID))
                    .noneMatch(c -> c.getName().equals("Deferred Sorcery"));
        }

        @Test
        @DisplayName("ShuffleIntoLibraryEffect shuffles spell into library")
        void shuffleIntoLibraryEffect() {
            Card card = createSorcery("Shuffle Sorcery");
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of(new ShuffleIntoLibraryEffect()));
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            assertThat(gd.playerDecks.get(PLAYER1_ID))
                    .anyMatch(c -> c.getName().equals("Shuffle Sorcery"));
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
        }

        @Test
        @DisplayName("Copy of a spell ceases to exist (does not go to graveyard)")
        void copyCeasesToExist() {
            Card card = createInstant("Copied Spell");
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of());
            entry.setCopy(true);
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
            assertThat(gd.playerExiledCards.get(PLAYER1_ID))
                    .noneMatch(c -> c.getName().equals("Copied Spell"));
        }

        @Test
        @DisplayName("Activated ability resolves without going to graveyard")
        void activatedAbilityDoesNotGoToGraveyard() {
            Card card = createCreature("Ability Source");
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, card,
                    PLAYER1_ID, "Ability Source's ability", List.of());
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
        }

        @Test
        @DisplayName("End turn requested exiles the resolving spell")
        void endTurnRequestedExilesSpell() {
            Card card = createSorcery("End Turn Sorcery");
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of());
            gd.stack.addLast(entry);
            gd.endTurnRequested = true;

            svc.resolveTopOfStack(gd);

            verify(exileService).exileCard(gd, PLAYER1_ID, card);
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
            assertThat(gd.endTurnRequested).isFalse();
        }

        @Test
        @DisplayName("End turn requested with a copy does not exile (ceases to exist)")
        void endTurnRequestedCopyCeasesToExist() {
            Card card = createInstant("End Turn Copy");
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of());
            entry.setCopy(true);
            gd.stack.addLast(entry);
            gd.endTurnRequested = true;

            svc.resolveTopOfStack(gd);

            verify(exileService, never()).exileCard(any(), any(), any());
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
        }

        @Test
        @DisplayName("Non-targeting spell does not fizzle even without valid target")
        void nonTargetingSpellDoesNotFizzle() {
            UUID nonExistentTarget = UUID.randomUUID();
            Card card = createSorcery("Non-Targeting Sorcery");
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, card,
                    PLAYER1_ID, card.getName(), List.of(), 0, nonExistentTarget, null);
            entry.setNonTargeting(true);
            gd.stack.addLast(entry);

            svc.resolveTopOfStack(gd);

            // Spell resolved normally and goes to graveyard (not fizzled)
            verify(effectResolutionService).resolveEffects(gd, entry);
            verify(graveyardService).addCardToGraveyard(gd, PLAYER1_ID, card);
        }
    }
}
