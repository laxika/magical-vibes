package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardAndUntapSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardUnlessAttackedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawAndLoseLifePerSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardsEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardsEqualToControlledCreatureCountEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardTransformIfCreatureDiscardedEffect;
import com.github.laxika.magicalvibes.model.effect.DrawXCardsEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRandomDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantPermanentNoMaxHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectDrawsEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCombatDamageLootEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndDrawCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnOwnPermanentTypeToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleHandIntoLibraryAndDrawEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsReturnSelfIfCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardOrControllerDrawsEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardXEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerDiscardsEffect;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingReturnToHandOnDiscardType;
import com.github.laxika.magicalvibes.model.effect.AddManaPerAttackingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaWithInstantSorceryCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToExileEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAndExileFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandToTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUnlessExileCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.DrawAndRandomDiscardWithSharedTypeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawXCardsForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardAndSameNameFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.FlipTwoCoinsEffect;
import com.github.laxika.magicalvibes.model.effect.PutAwakeningCountersOnTargetLandsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomHandCardAndPlayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactThenDealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesFromHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseFromListMessage;
import com.github.laxika.magicalvibes.networking.message.RevealHandMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerInteractionResolutionServiceTest {

    @Mock private DrawService drawService;
    @Mock private GraveyardService graveyardService;
    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private PlayerInputService playerInputService;
    @Mock private SessionManager sessionManager;
    @Mock private CardViewFactory cardViewFactory;
    @Mock private PermanentRemovalService permanentRemovalService;
    @Mock private BattlefieldEntryService battlefieldEntryService;
    @Mock private TriggerCollectionService triggerCollectionService;
    @Mock private EffectHandlerRegistry effectHandlerRegistry;

    @InjectMocks private PlayerInteractionResolutionService service;

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
        gd.playerHands.put(player1Id, new ArrayList<>());
        gd.playerHands.put(player2Id, new ArrayList<>());
        gd.playerGraveyards.put(player1Id, new ArrayList<>());
        gd.playerGraveyards.put(player2Id, new ArrayList<>());
        gd.playerDecks.put(player1Id, new ArrayList<>());
        gd.playerDecks.put(player2Id, new ArrayList<>());
        gd.playerLifeTotals.put(player1Id, 20);
        gd.playerLifeTotals.put(player2Id, 20);
        gd.playerManaPools.put(player1Id, new ManaPool());
        gd.playerManaPools.put(player2Id, new ManaPool());
        gd.activePlayerId = player1Id;
    }

    // ===== Helper methods =====

    private Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    private StackEntry createEntry(Card card, UUID controllerId, List<CardEffect> effects) {
        return new StackEntry(StackEntryType.SORCERY_SPELL, card, controllerId, card.getName(), effects);
    }

    private StackEntry createEntryWithTarget(Card card, UUID controllerId, List<CardEffect> effects, UUID targetId) {
        return new StackEntry(StackEntryType.SORCERY_SPELL, card, controllerId, card.getName(),
                effects, 0, targetId, null);
    }

    private StackEntry createEntryWithXValue(Card card, UUID controllerId, List<CardEffect> effects, int xValue) {
        return new StackEntry(StackEntryType.SORCERY_SPELL, card, controllerId, card.getName(), effects, xValue);
    }

    private StackEntry createEntryWithXValueAndTarget(Card card, UUID controllerId, List<CardEffect> effects, int xValue, UUID targetId) {
        return new StackEntry(StackEntryType.SORCERY_SPELL, card, controllerId, card.getName(),
                effects, xValue, targetId, null);
    }

    private StackEntry createTriggeredEntry(Card card, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) {
        return new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId, card.getName(), effects, null, sourcePermanentId);
    }

    private StackEntry createTriggeredEntryWithTarget(Card card, UUID controllerId, List<CardEffect> effects, UUID targetId, UUID sourcePermanentId) {
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId, card.getName(),
                effects, 0, targetId, sourcePermanentId, null, null, null, null);
        return entry;
    }

    // =========================================================================
    // DrawCardEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDrawCards")
    class ResolveDrawCards {

        @Test
        @DisplayName("Draws the specified number of cards for the controller")
        void drawsSpecifiedAmount() {
            Card card = createCard("Divination");
            DrawCardEffect effect = new DrawCardEffect(3);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveDrawCards(gd, entry, effect);

            verify(drawService, times(3)).resolveDrawCard(gd, player1Id);
        }

        @Test
        @DisplayName("Draws 1 card for single draw")
        void drawsSingleCard() {
            Card card = createCard("Opt");
            DrawCardEffect effect = new DrawCardEffect(1);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveDrawCards(gd, entry, effect);

            verify(drawService, times(1)).resolveDrawCard(gd, player1Id);
        }
    }

    // =========================================================================
    // EachPlayerDrawsCardEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveEachPlayerDrawsCard")
    class ResolveEachPlayerDrawsCard {

        @Test
        @DisplayName("Both players draw the specified number of cards")
        void bothPlayersDraw() {
            Card card = createCard("Howling Mine");
            EachPlayerDrawsCardEffect effect = new EachPlayerDrawsCardEffect(2);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveEachPlayerDrawsCard(gd, entry, effect);

            verify(drawService, times(2)).resolveDrawCard(gd, player1Id);
            verify(drawService, times(2)).resolveDrawCard(gd, player2Id);
        }

        @Test
        @DisplayName("Each player draws 1 card")
        void eachPlayerDrawsOne() {
            Card card = createCard("Temple Bell");
            EachPlayerDrawsCardEffect effect = new EachPlayerDrawsCardEffect(1);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveEachPlayerDrawsCard(gd, entry, effect);

            verify(drawService, times(1)).resolveDrawCard(gd, player1Id);
            verify(drawService, times(1)).resolveDrawCard(gd, player2Id);
        }
    }

    // =========================================================================
    // SacrificeSelfAndDrawCardsEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveSacrificeSelfAndDrawCards")
    class ResolveSacrificeSelfAndDrawCards {

        @Test
        @DisplayName("Sacrifices source and draws cards")
        void sacrificesSourceAndDraws() {
            Card card = createCard("Chromatic Star");
            Permanent source = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(source);

            SacrificeSelfAndDrawCardsEffect effect = new SacrificeSelfAndDrawCardsEffect(2);
            StackEntry entry = createTriggeredEntry(card, player1Id, List.of(effect), source.getId());

            when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);

            service.resolveSacrificeSelfAndDrawCards(gd, entry, effect);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, source);
            verify(drawService, times(2)).resolveDrawCard(gd, player1Id);
        }

        @Test
        @DisplayName("Does nothing when source permanent ID is null")
        void doesNothingWhenSourcePermanentIdNull() {
            Card card = createCard("Chromatic Star");
            SacrificeSelfAndDrawCardsEffect effect = new SacrificeSelfAndDrawCardsEffect(2);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveSacrificeSelfAndDrawCards(gd, entry, effect);

            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
            verify(drawService, never()).resolveDrawCard(any(), any());
        }

        @Test
        @DisplayName("Fizzles when source not found on battlefield")
        void fizzlesWhenSourceNotFound() {
            Card card = createCard("Chromatic Star");
            UUID fakePermanentId = UUID.randomUUID();
            SacrificeSelfAndDrawCardsEffect effect = new SacrificeSelfAndDrawCardsEffect(2);
            StackEntry entry = createTriggeredEntry(card, player1Id, List.of(effect), fakePermanentId);

            when(gameQueryService.findPermanentById(gd, fakePermanentId)).thenReturn(null);

            service.resolveSacrificeSelfAndDrawCards(gd, entry, effect);

            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
            verify(drawService, never()).resolveDrawCard(any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("fizzles")));
        }
    }

    // =========================================================================
    // DrawXCardsEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDrawXCards")
    class ResolveDrawXCards {

        @Test
        @DisplayName("Draws X cards based on xValue")
        void drawsXCards() {
            Card card = createCard("Stroke of Genius");
            DrawXCardsEffect effect = new DrawXCardsEffect();
            StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 5);

            service.resolveDrawXCards(gd, entry);

            verify(drawService, times(5)).resolveDrawCard(gd, player1Id);
        }

        @Test
        @DisplayName("Draws nothing when X is 0")
        void drawsNothingWhenXIsZero() {
            Card card = createCard("Stroke of Genius");
            DrawXCardsEffect effect = new DrawXCardsEffect();
            StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 0);

            service.resolveDrawXCards(gd, entry);

            verify(drawService, never()).resolveDrawCard(any(), any());
        }
    }

    // =========================================================================
    // ShuffleHandIntoLibraryAndDrawEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveShuffleHandIntoLibraryAndDraw")
    class ResolveShuffleHandIntoLibraryAndDraw {

        @Test
        @DisplayName("Shuffles hand into library and draws that many cards")
        void shufflesHandAndDraws() {
            Card card = createCard("Windfall");
            Card handCard1 = createCard("Mountain");
            Card handCard2 = createCard("Forest");
            Card handCard3 = createCard("Swamp");
            gd.playerHands.get(player1Id).addAll(List.of(handCard1, handCard2, handCard3));

            ShuffleHandIntoLibraryAndDrawEffect effect = new ShuffleHandIntoLibraryAndDrawEffect();
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveShuffleHandIntoLibraryAndDraw(gd, entry);

            // Hand should be cleared
            assertThat(gd.playerHands.get(player1Id)).isEmpty();
            // 3 cards moved to library
            assertThat(gd.playerDecks.get(player1Id)).hasSize(3);
            // Draw 3 cards (one per card shuffled)
            verify(drawService, times(3)).resolveDrawCard(gd, player1Id);
        }

        @Test
        @DisplayName("Skips player with empty hand")
        void skipsEmptyHand() {
            Card card = createCard("Windfall");
            ShuffleHandIntoLibraryAndDrawEffect effect = new ShuffleHandIntoLibraryAndDrawEffect();
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            // Both players have empty hands

            service.resolveShuffleHandIntoLibraryAndDraw(gd, entry);

            verify(drawService, never()).resolveDrawCard(any(), any());
            verify(gameBroadcastService, times(2)).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no cards in hand")));
        }

        @Test
        @DisplayName("Processes each player independently")
        void processesEachPlayer() {
            Card card = createCard("Windfall");
            gd.playerHands.get(player1Id).addAll(List.of(createCard("A"), createCard("B")));
            gd.playerHands.get(player2Id).addAll(List.of(createCard("C")));

            ShuffleHandIntoLibraryAndDrawEffect effect = new ShuffleHandIntoLibraryAndDrawEffect();
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveShuffleHandIntoLibraryAndDraw(gd, entry);

            verify(drawService, times(2)).resolveDrawCard(gd, player1Id);
            verify(drawService, times(1)).resolveDrawCard(gd, player2Id);
        }
    }

    // =========================================================================
    // DrawCardsEqualToControlledCreatureCountEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDrawCardsEqualToControlledCreatureCount")
    class ResolveDrawCardsEqualToControlledCreatureCount {

        @Test
        @DisplayName("Draws cards equal to controlled creature count")
        void drawsPerCreature() {
            Card card = createCard("Collective Unconscious");
            DrawCardsEqualToControlledCreatureCountEffect effect = new DrawCardsEqualToControlledCreatureCountEffect();
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            Permanent creature1 = new Permanent(createCard("Bear"));
            Permanent creature2 = new Permanent(createCard("Wolf"));
            gd.playerBattlefields.get(player1Id).addAll(List.of(creature1, creature2));

            when(gameQueryService.isCreature(gd, creature1)).thenReturn(true);
            when(gameQueryService.isCreature(gd, creature2)).thenReturn(true);

            service.resolveDrawCardsEqualToControlledCreatureCount(gd, entry);

            verify(drawService, times(2)).resolveDrawCard(gd, player1Id);
        }

        @Test
        @DisplayName("Draws 0 when no creatures on battlefield")
        void drawsNothingWhenNoCreatures() {
            Card card = createCard("Collective Unconscious");
            DrawCardsEqualToControlledCreatureCountEffect effect = new DrawCardsEqualToControlledCreatureCountEffect();
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveDrawCardsEqualToControlledCreatureCount(gd, entry);

            verify(drawService, never()).resolveDrawCard(any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("draws 0 cards") && msg.contains("no creatures")));
        }

        @Test
        @DisplayName("Only counts creatures, not non-creature permanents")
        void onlyCountsCreatures() {
            Card card = createCard("Collective Unconscious");
            DrawCardsEqualToControlledCreatureCountEffect effect = new DrawCardsEqualToControlledCreatureCountEffect();
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            Permanent creature = new Permanent(createCard("Bear"));
            Permanent artifact = new Permanent(createCard("Sol Ring"));
            gd.playerBattlefields.get(player1Id).addAll(List.of(creature, artifact));

            when(gameQueryService.isCreature(gd, creature)).thenReturn(true);
            when(gameQueryService.isCreature(gd, artifact)).thenReturn(false);

            service.resolveDrawCardsEqualToControlledCreatureCount(gd, entry);

            verify(drawService, times(1)).resolveDrawCard(gd, player1Id);
        }
    }

    // =========================================================================
    // DiscardCardEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDiscardCard")
    class ResolveDiscardCard {

        @Test
        @DisplayName("Sets discardCausedByOpponent to false and begins discard")
        void setsDiscardFlag() {
            Card card = createCard("Raven's Crime");
            DiscardCardEffect effect = new DiscardCardEffect(1);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.playerHands.get(player1Id).add(createCard("Mountain"));

            service.resolveDiscardCard(gd, entry, effect);

            assertThat(gd.discardCausedByOpponent).isFalse();
            verify(playerInputService).beginDiscardChoice(gd, player1Id);
        }

        @Test
        @DisplayName("Sets discard remaining count correctly")
        void setsDiscardCount() {
            Card card = createCard("Mind Rot");
            DiscardCardEffect effect = new DiscardCardEffect(2);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.playerHands.get(player1Id).addAll(List.of(createCard("A"), createCard("B")));

            service.resolveDiscardCard(gd, entry, effect);

            assertThat(gd.interaction.revealedHandChoice().discardRemainingCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Logs message when hand is empty")
        void logsWhenHandEmpty() {
            Card card = createCard("Raven's Crime");
            DiscardCardEffect effect = new DiscardCardEffect(1);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveDiscardCard(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no cards to discard")));
            verify(playerInputService, never()).beginDiscardChoice(any(), any());
        }
    }

    // =========================================================================
    // DiscardCardUnlessAttackedThisTurnEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDiscardCardUnlessAttackedThisTurn")
    class ResolveDiscardCardUnlessAttackedThisTurn {

        @Test
        @DisplayName("Skips discard when player attacked this turn")
        void skipsDiscardWhenAttacked() {
            Card card = createCard("Keldon Marauders");
            DiscardCardUnlessAttackedThisTurnEffect effect = new DiscardCardUnlessAttackedThisTurnEffect();
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.playersDeclaredAttackersThisTurn.add(player1Id);

            service.resolveDiscardCardUnlessAttackedThisTurn(gd, entry);

            verify(playerInputService, never()).beginDiscardChoice(any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("attacked this turn")));
        }

        @Test
        @DisplayName("Forces discard when player did not attack")
        void forcesDiscardWhenDidNotAttack() {
            Card card = createCard("Keldon Marauders");
            DiscardCardUnlessAttackedThisTurnEffect effect = new DiscardCardUnlessAttackedThisTurnEffect();
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.playerHands.get(player1Id).add(createCard("Mountain"));

            service.resolveDiscardCardUnlessAttackedThisTurn(gd, entry);

            assertThat(gd.discardCausedByOpponent).isFalse();
            verify(playerInputService).beginDiscardChoice(gd, player1Id);
        }
    }

    // =========================================================================
    // DiscardOwnHandEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDiscardOwnHand")
    class ResolveDiscardOwnHand {

        @Test
        @DisplayName("Discards all cards from hand to graveyard")
        void discardsAllCards() {
            Card card = createCard("One with Nothing");
            Card handCard1 = createCard("Mountain");
            Card handCard2 = createCard("Forest");
            gd.playerHands.get(player1Id).addAll(List.of(handCard1, handCard2));
            StackEntry entry = createEntry(card, player1Id, List.of(new DiscardOwnHandEffect()));

            service.resolveDiscardOwnHand(gd, entry);

            assertThat(gd.playerHands.get(player1Id)).isEmpty();
            verify(graveyardService).addCardToGraveyard(gd, player1Id, handCard1);
            verify(graveyardService).addCardToGraveyard(gd, player1Id, handCard2);
            verify(triggerCollectionService).checkDiscardTriggers(gd, player1Id, handCard1);
            verify(triggerCollectionService).checkDiscardTriggers(gd, player1Id, handCard2);
        }

        @Test
        @DisplayName("Does nothing with empty hand")
        void doesNothingWithEmptyHand() {
            Card card = createCard("One with Nothing");
            StackEntry entry = createEntry(card, player1Id, List.of(new DiscardOwnHandEffect()));

            service.resolveDiscardOwnHand(gd, entry);

            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no cards to discard")));
        }

        @Test
        @DisplayName("Sets discardCausedByOpponent to false")
        void setsDiscardCausedByOpponent() {
            Card card = createCard("One with Nothing");
            gd.playerHands.get(player1Id).add(createCard("Mountain"));
            gd.discardCausedByOpponent = true;
            StackEntry entry = createEntry(card, player1Id, List.of(new DiscardOwnHandEffect()));

            service.resolveDiscardOwnHand(gd, entry);

            assertThat(gd.discardCausedByOpponent).isFalse();
        }

        @Test
        @DisplayName("Logs discard count correctly")
        void logsDiscardCount() {
            Card card = createCard("One with Nothing");
            gd.playerHands.get(player1Id).addAll(List.of(createCard("A"), createCard("B"), createCard("C")));
            StackEntry entry = createEntry(card, player1Id, List.of(new DiscardOwnHandEffect()));

            service.resolveDiscardOwnHand(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("discards their hand") && msg.contains("3 cards")));
        }
    }

    // =========================================================================
    // DrawAndDiscardCardEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDrawAndDiscard")
    class ResolveDrawAndDiscard {

        @Test
        @DisplayName("Draws then sets up discard")
        void drawsThenDiscards() {
            Card card = createCard("Faithless Looting");
            DrawAndDiscardCardEffect effect = new DrawAndDiscardCardEffect(2, 1);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.playerHands.get(player1Id).add(createCard("Mountain"));

            service.resolveDrawAndDiscard(gd, entry, effect);

            verify(drawService, times(2)).resolveDrawCard(gd, player1Id);
            assertThat(gd.discardCausedByOpponent).isFalse();
            verify(playerInputService).beginDiscardChoice(gd, player1Id);
        }
    }

    // =========================================================================
    // TargetPlayerDiscardsEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveTargetPlayerDiscards")
    class ResolveTargetPlayerDiscards {

        @Test
        @DisplayName("Target player discards with opponent flag set")
        void targetPlayerDiscardsWithOpponentFlag() {
            Card card = createCard("Mind Rot");
            TargetPlayerDiscardsEffect effect = new TargetPlayerDiscardsEffect(2);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
            gd.playerHands.get(player2Id).addAll(List.of(createCard("A"), createCard("B")));

            service.resolveTargetPlayerDiscards(gd, entry, effect);

            assertThat(gd.discardCausedByOpponent).isTrue();
            verify(playerInputService).beginDiscardChoice(gd, player2Id);
        }

        @Test
        @DisplayName("Logs when target has empty hand")
        void logsWhenTargetHandEmpty() {
            Card card = createCard("Mind Rot");
            TargetPlayerDiscardsEffect effect = new TargetPlayerDiscardsEffect(2);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            service.resolveTargetPlayerDiscards(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no cards to discard")));
        }
    }

    // =========================================================================
    // TargetSpellControllerDiscardsEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveTargetSpellControllerDiscards")
    class ResolveTargetSpellControllerDiscards {

        @Test
        @DisplayName("Discards for the controller of the targeted spell")
        void discardsForSpellController() {
            Card sourceCard = createCard("Counterspell");
            Card targetSpellCard = createCard("Lightning Bolt");

            StackEntry targetSpell = new StackEntry(StackEntryType.INSTANT_SPELL, targetSpellCard,
                    player2Id, targetSpellCard.getName(), List.of());
            gd.stack.add(targetSpell);

            TargetSpellControllerDiscardsEffect effect = new TargetSpellControllerDiscardsEffect(1);
            StackEntry entry = createEntryWithTarget(sourceCard, player1Id, List.of(effect), targetSpellCard.getId());
            gd.playerHands.get(player2Id).add(createCard("Mountain"));

            service.resolveTargetSpellControllerDiscards(gd, entry, effect);

            assertThat(gd.discardCausedByOpponent).isTrue();
            verify(playerInputService).beginDiscardChoice(gd, player2Id);
        }

        @Test
        @DisplayName("Does nothing when target spell not on stack")
        void doesNothingWhenTargetNotOnStack() {
            Card sourceCard = createCard("Counterspell");
            TargetSpellControllerDiscardsEffect effect = new TargetSpellControllerDiscardsEffect(1);
            StackEntry entry = createEntryWithTarget(sourceCard, player1Id, List.of(effect), UUID.randomUUID());

            service.resolveTargetSpellControllerDiscards(gd, entry, effect);

            verify(playerInputService, never()).beginDiscardChoice(any(), any());
        }

        @Test
        @DisplayName("Does nothing when target ID is null")
        void doesNothingWhenTargetIdNull() {
            Card sourceCard = createCard("Counterspell");
            TargetSpellControllerDiscardsEffect effect = new TargetSpellControllerDiscardsEffect(1);
            StackEntry entry = createEntry(sourceCard, player1Id, List.of(effect));

            service.resolveTargetSpellControllerDiscards(gd, entry, effect);

            verify(playerInputService, never()).beginDiscardChoice(any(), any());
        }
    }

    // =========================================================================
    // TargetPlayerDiscardsByChargeCountersEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveTargetPlayerDiscardsByChargeCounters")
    class ResolveTargetPlayerDiscardsByChargeCounters {

        @Test
        @DisplayName("Discards cards equal to charge counter count")
        void discardsBasedOnChargeCounters() {
            Card card = createCard("Shrine of Limitless Power");
            TargetPlayerDiscardsByChargeCountersEffect effect = new TargetPlayerDiscardsByChargeCountersEffect();
            StackEntry entry = createEntryWithXValueAndTarget(card, player1Id, List.of(effect), 3, player2Id);
            gd.playerHands.get(player2Id).addAll(List.of(createCard("A"), createCard("B"), createCard("C")));

            service.resolveTargetPlayerDiscardsByChargeCounters(gd, entry);

            assertThat(gd.discardCausedByOpponent).isTrue();
            verify(playerInputService).beginDiscardChoice(gd, player2Id);
        }

        @Test
        @DisplayName("No discard when charge counter value is 0")
        void noDiscardWhenZeroChargeCounters() {
            Card card = createCard("Shrine of Limitless Power");
            TargetPlayerDiscardsByChargeCountersEffect effect = new TargetPlayerDiscardsByChargeCountersEffect();
            StackEntry entry = createEntryWithXValueAndTarget(card, player1Id, List.of(effect), 0, player2Id);

            service.resolveTargetPlayerDiscardsByChargeCounters(gd, entry);

            verify(playerInputService, never()).beginDiscardChoice(any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("discards 0 cards")));
        }
    }

    // =========================================================================
    // EachPlayerDiscardsEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveEachPlayerDiscards")
    class ResolveEachPlayerDiscards {

        @Test
        @DisplayName("APNAP order: active player is first in the queue")
        void activePlayerFirst() {
            Card card = createCard("Syphon Mind");
            EachPlayerDiscardsEffect effect = new EachPlayerDiscardsEffect(1);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.activePlayerId = player1Id;
            gd.playerHands.get(player1Id).add(createCard("A"));

            service.resolveEachPlayerDiscards(gd, entry, effect);

            // Active player starts the discard
            verify(playerInputService).beginDiscardChoice(gd, player1Id);
            assertThat(gd.pendingEachPlayerDiscardAmount).isEqualTo(1);
        }

        @Test
        @DisplayName("Opponent is in queue after active player")
        void opponentInQueueAfterActivePlayer() {
            Card card = createCard("Syphon Mind");
            EachPlayerDiscardsEffect effect = new EachPlayerDiscardsEffect(1);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.activePlayerId = player1Id;
            gd.playerHands.get(player1Id).add(createCard("A"));
            gd.playerHands.get(player2Id).add(createCard("B"));

            service.resolveEachPlayerDiscards(gd, entry, effect);

            // player2 should be in the pending queue
            assertThat(gd.pendingEachPlayerDiscardQueue).containsExactly(player2Id);
        }

        @Test
        @DisplayName("Stores controller ID for opponent detection")
        void storesControllerId() {
            Card card = createCard("Syphon Mind");
            EachPlayerDiscardsEffect effect = new EachPlayerDiscardsEffect(1);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.playerHands.get(player1Id).add(createCard("A"));

            service.resolveEachPlayerDiscards(gd, entry, effect);

            assertThat(gd.pendingEachPlayerDiscardControllerId).isEqualTo(player1Id);
        }
    }

    // =========================================================================
    // EachOpponentDiscardsEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveEachOpponentDiscards")
    class ResolveEachOpponentDiscards {

        @Test
        @DisplayName("Only opponents discard, controller is excluded")
        void onlyOpponentsDiscard() {
            Card card = createCard("Hymn to Tourach");
            EachOpponentDiscardsEffect effect = new EachOpponentDiscardsEffect(2);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.activePlayerId = player2Id;
            gd.playerHands.get(player2Id).addAll(List.of(createCard("A"), createCard("B")));

            service.resolveEachOpponentDiscards(gd, entry, effect);

            // Player2 (opponent) should be first since they're active player and not controller
            verify(playerInputService).beginDiscardChoice(gd, player2Id);
            assertThat(gd.pendingEachPlayerDiscardAmount).isEqualTo(2);
        }

        @Test
        @DisplayName("Controller is never added to discard queue")
        void controllerExcludedFromQueue() {
            Card card = createCard("Hymn to Tourach");
            EachOpponentDiscardsEffect effect = new EachOpponentDiscardsEffect(1);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.activePlayerId = player1Id;
            gd.playerHands.get(player2Id).add(createCard("A"));

            service.resolveEachOpponentDiscards(gd, entry, effect);

            // player2 is the only opponent; active player is controller, so they start
            // with player2 from queue
            verify(playerInputService).beginDiscardChoice(gd, player2Id);
        }
    }

    // =========================================================================
    // startNextEachPlayerDiscard (public)
    // =========================================================================

    @Nested
    @DisplayName("startNextEachPlayerDiscard")
    class StartNextEachPlayerDiscard {

        @Test
        @DisplayName("Begins discard for next player in queue")
        void beginsDiscardForNextPlayer() {
            gd.pendingEachPlayerDiscardQueue.add(player2Id);
            gd.pendingEachPlayerDiscardAmount = 1;
            gd.pendingEachPlayerDiscardControllerId = player1Id;
            gd.playerHands.get(player2Id).add(createCard("Mountain"));

            service.startNextEachPlayerDiscard(gd);

            verify(playerInputService).beginDiscardChoice(gd, player2Id);
            assertThat(gd.discardCausedByOpponent).isTrue();
        }

        @Test
        @DisplayName("Skips players with empty hands")
        void skipsPlayersWithEmptyHands() {
            gd.pendingEachPlayerDiscardQueue.add(player1Id);
            gd.pendingEachPlayerDiscardQueue.add(player2Id);
            gd.pendingEachPlayerDiscardAmount = 1;
            gd.pendingEachPlayerDiscardControllerId = player1Id;
            // player1 has empty hand, player2 has cards
            gd.playerHands.get(player2Id).add(createCard("Mountain"));

            service.startNextEachPlayerDiscard(gd);

            // player1 skipped, player2 gets the discard prompt
            verify(playerInputService).beginDiscardChoice(gd, player2Id);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("Player1") && msg.contains("no cards to discard")));
        }

        @Test
        @DisplayName("Clears controller tracking when all players done")
        void clearsControllerWhenDone() {
            gd.pendingEachPlayerDiscardControllerId = player1Id;
            // Empty queue — all players already processed

            service.startNextEachPlayerDiscard(gd);

            assertThat(gd.pendingEachPlayerDiscardControllerId).isNull();
            verify(playerInputService, never()).beginDiscardChoice(any(), any());
        }

        @Test
        @DisplayName("Controller's own discard is not marked as opponent-caused")
        void controllerDiscardNotOpponentCaused() {
            gd.pendingEachPlayerDiscardQueue.add(player1Id);
            gd.pendingEachPlayerDiscardAmount = 1;
            gd.pendingEachPlayerDiscardControllerId = player1Id;
            gd.playerHands.get(player1Id).add(createCard("Mountain"));

            service.startNextEachPlayerDiscard(gd);

            assertThat(gd.discardCausedByOpponent).isFalse();
        }
    }

    // =========================================================================
    // TargetPlayerRandomDiscardOrControllerDrawsEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveTargetPlayerRandomDiscardOrControllerDraws")
    class ResolveTargetPlayerRandomDiscardOrControllerDraws {

        @Test
        @DisplayName("Controller draws a card when target's hand is empty")
        void controllerDrawsWhenTargetHandEmpty() {
            Card card = createCard("Blazing Specter");
            TargetPlayerRandomDiscardOrControllerDrawsEffect effect = new TargetPlayerRandomDiscardOrControllerDrawsEffect();
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            service.resolveTargetPlayerRandomDiscardOrControllerDraws(gd, entry);

            verify(drawService).resolveDrawCard(gd, player1Id);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no cards to discard") && msg.contains("draws a card")));
        }

        @Test
        @DisplayName("Target player discards at random when hand is not empty")
        void targetDiscardsWhenHandNotEmpty() {
            Card card = createCard("Blazing Specter");
            TargetPlayerRandomDiscardOrControllerDrawsEffect effect = new TargetPlayerRandomDiscardOrControllerDrawsEffect();
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
            Card handCard = createCard("Mountain");
            gd.playerHands.get(player2Id).add(handCard);

            service.resolveTargetPlayerRandomDiscardOrControllerDraws(gd, entry);

            assertThat(gd.discardCausedByOpponent).isTrue();
            // Random discard removes from hand and sends to graveyard
            assertThat(gd.playerHands.get(player2Id)).isEmpty();
            verify(graveyardService).addCardToGraveyard(eq(gd), eq(player2Id), any());
        }
    }

    // =========================================================================
    // TargetPlayerRandomDiscardXEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveTargetPlayerRandomDiscardX")
    class ResolveTargetPlayerRandomDiscardX {

        @Test
        @DisplayName("No discard when X is 0")
        void noDiscardWhenXIsZero() {
            Card card = createCard("Mind Shatter");
            TargetPlayerRandomDiscardXEffect effect = new TargetPlayerRandomDiscardXEffect();
            StackEntry entry = createEntryWithXValueAndTarget(card, player1Id, List.of(effect), 0, player2Id);

            service.resolveTargetPlayerRandomDiscardX(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("discards 0 cards")));
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
        }

        @Test
        @DisplayName("Discards X cards at random")
        void discardsXCardsAtRandom() {
            Card card = createCard("Mind Shatter");
            TargetPlayerRandomDiscardXEffect effect = new TargetPlayerRandomDiscardXEffect();
            StackEntry entry = createEntryWithXValueAndTarget(card, player1Id, List.of(effect), 2, player2Id);
            gd.playerHands.get(player2Id).addAll(List.of(createCard("A"), createCard("B"), createCard("C")));

            service.resolveTargetPlayerRandomDiscardX(gd, entry);

            assertThat(gd.discardCausedByOpponent).isTrue();
            // 2 cards should have been discarded
            assertThat(gd.playerHands.get(player2Id)).hasSize(1);
            verify(graveyardService, times(2)).addCardToGraveyard(eq(gd), eq(player2Id), any());
        }
    }

    // =========================================================================
    // LookAtHandEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveLookAtHand")
    class ResolveLookAtHand {

        @Test
        @DisplayName("Reveals hand contents to controller")
        void revealsHandToController() {
            Card card = createCard("Telepathy");
            LookAtHandEffect effect = new LookAtHandEffect();
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
            Card handCard = createCard("Mountain");
            gd.playerHands.get(player2Id).add(handCard);

            CardView mockView = mock(CardView.class);
            when(cardViewFactory.create(handCard)).thenReturn(mockView);

            service.resolveLookAtHand(gd, entry);

            verify(sessionManager).sendToPlayer(eq(player1Id), any(RevealHandMessage.class));
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("looks at") && msg.contains("Player2") && msg.contains("Mountain")));
        }

        @Test
        @DisplayName("Handles empty hand")
        void handlesEmptyHand() {
            Card card = createCard("Telepathy");
            LookAtHandEffect effect = new LookAtHandEffect();
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            service.resolveLookAtHand(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("looks at") && msg.contains("empty")));
        }
    }

    // =========================================================================
    // RedirectDrawsEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveRedirectDraws")
    class ResolveRedirectDraws {

        @Test
        @DisplayName("Sets up draw replacement mapping")
        void setsUpDrawReplacement() {
            Card card = createCard("Notion Thief");
            RedirectDrawsEffect effect = new RedirectDrawsEffect();
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            service.resolveRedirectDraws(gd, entry);

            assertThat(gd.drawReplacementTargetToController).containsEntry(player2Id, player1Id);
        }

        @Test
        @DisplayName("Does nothing when target player not found")
        void doesNothingWhenTargetNotFound() {
            Card card = createCard("Notion Thief");
            RedirectDrawsEffect effect = new RedirectDrawsEffect();
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), UUID.randomUUID());

            service.resolveRedirectDraws(gd, entry);

            assertThat(gd.drawReplacementTargetToController).isEmpty();
        }
    }

    // =========================================================================
    // DrawCardForTargetPlayerEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDrawCardForTargetPlayer")
    class ResolveDrawCardForTargetPlayer {

        @Test
        @DisplayName("Draws cards for target player")
        void drawsForTargetPlayer() {
            Card card = createCard("Temple Bell");
            DrawCardForTargetPlayerEffect effect = new DrawCardForTargetPlayerEffect(2, false, true);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            service.resolveDrawCardForTargetPlayer(gd, entry, effect);

            verify(drawService, times(2)).resolveDrawCard(gd, player2Id);
        }

        @Test
        @DisplayName("Does nothing when source is tapped and requireSourceUntapped is true")
        void doesNothingWhenSourceTapped() {
            Card card = createCard("Archivist");
            Permanent source = new Permanent(card);
            source.tap();

            DrawCardForTargetPlayerEffect effect = new DrawCardForTargetPlayerEffect(1, true, true);
            StackEntry entry = createTriggeredEntryWithTarget(card, player1Id, List.of(effect), player2Id, source.getId());

            when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);

            service.resolveDrawCardForTargetPlayer(gd, entry, effect);

            verify(drawService, never()).resolveDrawCard(any(), any());
        }

        @Test
        @DisplayName("Still draws when source left battlefield (uses last known info)")
        void stillDrawsWhenSourceLeftBattlefield() {
            Card card = createCard("Archivist");
            UUID sourcePermanentId = UUID.randomUUID();

            DrawCardForTargetPlayerEffect effect = new DrawCardForTargetPlayerEffect(1, true, true);
            StackEntry entry = createTriggeredEntryWithTarget(card, player1Id, List.of(effect), player2Id, sourcePermanentId);

            when(gameQueryService.findPermanentById(gd, sourcePermanentId)).thenReturn(null);

            service.resolveDrawCardForTargetPlayer(gd, entry, effect);

            verify(drawService, times(1)).resolveDrawCard(gd, player2Id);
        }

        @Test
        @DisplayName("Draws when source is untapped and requireSourceUntapped is true")
        void drawsWhenSourceUntapped() {
            Card card = createCard("Archivist");
            Permanent source = new Permanent(card);

            DrawCardForTargetPlayerEffect effect = new DrawCardForTargetPlayerEffect(1, true, true);
            StackEntry entry = createTriggeredEntryWithTarget(card, player1Id, List.of(effect), player2Id, source.getId());

            when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);

            service.resolveDrawCardForTargetPlayer(gd, entry, effect);

            verify(drawService, times(1)).resolveDrawCard(gd, player2Id);
        }
    }

    // =========================================================================
    // GrantPermanentNoMaxHandSizeEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveGrantPermanentNoMaxHandSize")
    class ResolveGrantPermanentNoMaxHandSize {

        @Test
        @DisplayName("Adds player to no max hand size set")
        void addsPlayerToSet() {
            Card card = createCard("Spellbook");
            GrantPermanentNoMaxHandSizeEffect effect = new GrantPermanentNoMaxHandSizeEffect();
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveGrantPermanentNoMaxHandSize(gd, entry);

            assertThat(gd.playersWithNoMaximumHandSize).contains(player1Id);
        }

        @Test
        @DisplayName("Logs the no max hand size grant")
        void logsGrant() {
            Card card = createCard("Spellbook");
            GrantPermanentNoMaxHandSizeEffect effect = new GrantPermanentNoMaxHandSizeEffect();
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveGrantPermanentNoMaxHandSize(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no maximum hand size")));
        }
    }

    // =========================================================================
    // DrawAndLoseLifePerSubtypeEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDrawAndLoseLifePerSubtype")
    class ResolveDrawAndLoseLifePerSubtype {

        @Test
        @DisplayName("Draws and loses life equal to subtype count")
        void drawsAndLosesLifePerSubtype() {
            Card card = createCard("Graveborn Muse");
            DrawAndLoseLifePerSubtypeEffect effect = new DrawAndLoseLifePerSubtypeEffect(CardSubtype.ZOMBIE);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            Card zombie1Card = createCard("Zombie 1");
            zombie1Card.setSubtypes(new ArrayList<>(List.of(CardSubtype.ZOMBIE)));
            Card zombie2Card = createCard("Zombie 2");
            zombie2Card.setSubtypes(new ArrayList<>(List.of(CardSubtype.ZOMBIE)));
            Permanent zombie1 = new Permanent(zombie1Card);
            Permanent zombie2 = new Permanent(zombie2Card);
            gd.playerBattlefields.get(player1Id).addAll(List.of(zombie1, zombie2));

            when(gameQueryService.canPlayerLifeChange(gd, player1Id)).thenReturn(true);

            service.resolveDrawAndLoseLifePerSubtype(gd, entry, effect);

            verify(drawService, times(2)).resolveDrawCard(gd, player1Id);
            assertThat(gd.playerLifeTotals.get(player1Id)).isEqualTo(18);
        }

        @Test
        @DisplayName("No effect when no creatures of subtype")
        void noEffectWhenNoSubtype() {
            Card card = createCard("Graveborn Muse");
            DrawAndLoseLifePerSubtypeEffect effect = new DrawAndLoseLifePerSubtypeEffect(CardSubtype.ZOMBIE);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveDrawAndLoseLifePerSubtype(gd, entry, effect);

            verify(drawService, never()).resolveDrawCard(any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("controls no") && msg.contains("Zombie")));
        }

        @Test
        @DisplayName("Draws but does not lose life when life can't change")
        void drawsButNoLifeLossWhenCantChange() {
            Card card = createCard("Graveborn Muse");
            DrawAndLoseLifePerSubtypeEffect effect = new DrawAndLoseLifePerSubtypeEffect(CardSubtype.ZOMBIE);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            Card zombieCard = createCard("Zombie");
            zombieCard.setSubtypes(new ArrayList<>(List.of(CardSubtype.ZOMBIE)));
            Permanent zombie = new Permanent(zombieCard);
            gd.playerBattlefields.get(player1Id).add(zombie);

            when(gameQueryService.canPlayerLifeChange(gd, player1Id)).thenReturn(false);

            service.resolveDrawAndLoseLifePerSubtype(gd, entry, effect);

            verify(drawService, times(1)).resolveDrawCard(gd, player1Id);
            assertThat(gd.playerLifeTotals.get(player1Id)).isEqualTo(20);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("life total can't change")));
        }
    }

    // =========================================================================
    // LoseLifeUnlessDiscardEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveLoseLifeUnlessDiscard")
    class ResolveLoseLifeUnlessDiscard {

        @Test
        @DisplayName("Auto-applies life loss when no cards in hand")
        void autoAppliesLifeLossWhenNoCards() {
            Card card = createCard("Rackling");
            LoseLifeUnlessDiscardEffect effect = new LoseLifeUnlessDiscardEffect(3);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(true);

            service.resolveLoseLifeUnlessDiscard(gd, entry, effect);

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(17);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no cards to discard") && msg.contains("loses 3 life")));
        }

        @Test
        @DisplayName("No life loss when life can't change and no cards")
        void noLifeLossWhenLifeCantChange() {
            Card card = createCard("Rackling");
            LoseLifeUnlessDiscardEffect effect = new LoseLifeUnlessDiscardEffect(3);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(false);

            service.resolveLoseLifeUnlessDiscard(gd, entry, effect);

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
        }

        @Test
        @DisplayName("Presents may ability when cards are available")
        void presentsMayAbilityWhenCardsAvailable() {
            Card card = createCard("Rackling");
            LoseLifeUnlessDiscardEffect effect = new LoseLifeUnlessDiscardEffect(3);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
            gd.playerHands.get(player2Id).add(createCard("Mountain"));

            service.resolveLoseLifeUnlessDiscard(gd, entry, effect);

            assertThat(gd.pendingMayAbilities).isNotEmpty();
            assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player2Id);
        }
    }

    // =========================================================================
    // LoseLifeUnlessPaysEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveLoseLifeUnlessPays")
    class ResolveLoseLifeUnlessPays {

        @Test
        @DisplayName("Auto-applies life loss when can't pay")
        void autoAppliesLifeLossWhenCantPay() {
            Card card = createCard("Rhystic Study");
            LoseLifeUnlessPaysEffect effect = new LoseLifeUnlessPaysEffect(2, 1, null);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(true);

            service.resolveLoseLifeUnlessPays(gd, entry, effect);

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(18);
        }

        @Test
        @DisplayName("No life loss when life can't change and can't pay")
        void noLifeLossWhenLifeCantChange() {
            Card card = createCard("Rhystic Study");
            LoseLifeUnlessPaysEffect effect = new LoseLifeUnlessPaysEffect(2, 1, null);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(false);

            service.resolveLoseLifeUnlessPays(gd, entry, effect);

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
        }

        @Test
        @DisplayName("Presents may ability when player can pay")
        void presentsMayAbilityWhenCanPay() {
            Card card = createCard("Rhystic Study");
            LoseLifeUnlessPaysEffect effect = new LoseLifeUnlessPaysEffect(2, 1, null);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
            // Add enough mana to pay {1}
            gd.playerManaPools.get(player2Id).add(com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);

            service.resolveLoseLifeUnlessPays(gd, entry, effect);

            assertThat(gd.pendingMayAbilities).isNotEmpty();
            assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player2Id);
            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
        }
    }

    // =========================================================================
    // DrawXCardsForTargetPlayerEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDrawXCardsForTargetPlayer")
    class ResolveDrawXCardsForTargetPlayer {

        @Test
        @DisplayName("Target player draws X cards")
        void targetPlayerDrawsXCards() {
            Card card = createCard("Blue Sun's Zenith");
            DrawXCardsEffect effect = new DrawXCardsEffect();
            StackEntry entry = createEntryWithXValueAndTarget(card, player1Id, List.of(effect), 4, player2Id);

            service.resolveDrawXCardsForTargetPlayer(gd, entry);

            verify(drawService, times(4)).resolveDrawCard(gd, player2Id);
        }

        @Test
        @DisplayName("Logs draw message")
        void logsDrawMessage() {
            Card card = createCard("Blue Sun's Zenith");
            DrawXCardsEffect effect = new DrawXCardsEffect();
            StackEntry entry = createEntryWithXValueAndTarget(card, player1Id, List.of(effect), 3, player2Id);

            service.resolveDrawXCardsForTargetPlayer(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("Player2") && msg.contains("draws 3 cards")));
        }
    }

    // =========================================================================
    // DrawCardsEqualToChargeCountersOnSourceEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDrawCardsEqualToChargeCounters")
    class ResolveDrawCardsEqualToChargeCounters {

        @Test
        @DisplayName("Draws cards equal to xValue (charge counters)")
        void drawsEqualToChargeCounters() {
            Card card = createCard("Shrine of Piercing Vision");
            DrawCardsEqualToChargeCountersOnSourceEffect effect = new DrawCardsEqualToChargeCountersOnSourceEffect();
            StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 4);

            service.resolveDrawCardsEqualToChargeCounters(gd, entry);

            verify(drawService, times(4)).resolveDrawCard(gd, player1Id);
        }

        @Test
        @DisplayName("Draws 0 when no charge counters")
        void drawsZeroWhenNoChargeCounters() {
            Card card = createCard("Shrine of Piercing Vision");
            DrawCardsEqualToChargeCountersOnSourceEffect effect = new DrawCardsEqualToChargeCountersOnSourceEffect();
            StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 0);

            service.resolveDrawCardsEqualToChargeCounters(gd, entry);

            verify(drawService, never()).resolveDrawCard(any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("draws 0 cards") && msg.contains("no charge counters")));
        }
    }

    // =========================================================================
    // DiscardAndDrawCardEffect (rummage — discard first, then draw)
    // =========================================================================

    @Nested
    @DisplayName("resolveDiscardAndDraw")
    class ResolveDiscardAndDraw {

        @Test
        @DisplayName("Begins discard then stores pending draw count")
        void beginsDiscardAndStoresDrawCount() {
            Card card = createCard("Faithless Looting");
            DiscardAndDrawCardEffect effect = new DiscardAndDrawCardEffect(1, 2);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.playerHands.get(player1Id).add(createCard("Mountain"));

            service.resolveDiscardAndDraw(gd, entry, effect);

            assertThat(gd.pendingRummageDrawCount).isEqualTo(2);
            assertThat(gd.discardCausedByOpponent).isFalse();
            verify(playerInputService).beginDiscardChoice(gd, player1Id);
        }

        @Test
        @DisplayName("Does nothing when hand is empty")
        void doesNothingWhenHandEmpty() {
            Card card = createCard("Faithless Looting");
            DiscardAndDrawCardEffect effect = new DiscardAndDrawCardEffect(1, 2);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveDiscardAndDraw(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no cards to discard")));
            verify(playerInputService, never()).beginDiscardChoice(any(), any());
        }
    }

    // =========================================================================
    // DiscardCardAndUntapSelfEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDiscardAndUntapSelf")
    class ResolveDiscardAndUntapSelf {

        @Test
        @DisplayName("Sets pending untap permanent ID and begins discard")
        void setsUntapAndBeginsDiscard() {
            Card card = createCard("Merfolk Looter");
            DiscardCardAndUntapSelfEffect effect = new DiscardCardAndUntapSelfEffect();
            UUID sourcePermanentId = UUID.randomUUID();
            StackEntry entry = createTriggeredEntry(card, player1Id, List.of(effect), sourcePermanentId);
            gd.playerHands.get(player1Id).add(createCard("Mountain"));

            service.resolveDiscardAndUntapSelf(gd, entry, effect);

            assertThat(gd.pendingUntapAfterDiscardPermanentId).isEqualTo(sourcePermanentId);
            assertThat(gd.discardCausedByOpponent).isFalse();
            verify(playerInputService).beginDiscardChoice(gd, player1Id);
        }

        @Test
        @DisplayName("Does nothing when hand is empty")
        void doesNothingWhenHandEmpty() {
            Card card = createCard("Merfolk Looter");
            DiscardCardAndUntapSelfEffect effect = new DiscardCardAndUntapSelfEffect();
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveDiscardAndUntapSelf(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no cards to discard")));
        }
    }

    // =========================================================================
    // DrawDiscardTransformIfCreatureDiscardedEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDrawDiscardTransformIfCreatureDiscarded")
    class ResolveDrawDiscardTransformIfCreatureDiscarded {

        @Test
        @DisplayName("Draws a card, sets pending transform, then begins discard")
        void drawsAndSetsTransform() {
            Card card = createCard("Civilized Scholar");
            DrawDiscardTransformIfCreatureDiscardedEffect effect = new DrawDiscardTransformIfCreatureDiscardedEffect();
            UUID sourcePermanentId = UUID.randomUUID();
            StackEntry entry = createTriggeredEntry(card, player1Id, List.of(effect), sourcePermanentId);
            gd.playerHands.get(player1Id).add(createCard("Mountain"));

            service.resolveDrawDiscardTransformIfCreatureDiscarded(gd, entry, effect);

            verify(drawService).resolveDrawCard(gd, player1Id);
            assertThat(gd.pendingTransformOnCreatureDiscard).isNotNull();
            assertThat(gd.pendingTransformOnCreatureDiscard.sourcePermanentId()).isEqualTo(sourcePermanentId);
            assertThat(gd.discardCausedByOpponent).isFalse();
            verify(playerInputService).beginDiscardChoice(gd, player1Id);
        }

        @Test
        @DisplayName("Does not set transform when source permanent ID is null")
        void noTransformWhenNullSourcePermanent() {
            Card card = createCard("Civilized Scholar");
            DrawDiscardTransformIfCreatureDiscardedEffect effect = new DrawDiscardTransformIfCreatureDiscardedEffect();
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.playerHands.get(player1Id).add(createCard("Mountain"));

            service.resolveDrawDiscardTransformIfCreatureDiscarded(gd, entry, effect);

            verify(drawService).resolveDrawCard(gd, player1Id);
            assertThat(gd.pendingTransformOnCreatureDiscard).isNull();
        }
    }

    // =========================================================================
    // RegisterDelayedCombatDamageLootEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveRegisterDelayedCombatDamageLoot")
    class ResolveRegisterDelayedCombatDamageLoot {

        @Test
        @DisplayName("Registers delayed loot trigger with correct values")
        void registersDelayedLoot() {
            Card card = createCard("Looter il-Kor");
            RegisterDelayedCombatDamageLootEffect effect = new RegisterDelayedCombatDamageLootEffect(1, 1);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveRegisterDelayedCombatDamageLoot(gd, entry, effect);

            assertThat(gd.pendingDelayedCombatDamageLoots).hasSize(1);
            GameData.DelayedCombatDamageLoot loot = gd.pendingDelayedCombatDamageLoots.getFirst();
            assertThat(loot.controllerId()).isEqualTo(player1Id);
            assertThat(loot.drawAmount()).isEqualTo(1);
            assertThat(loot.discardAmount()).isEqualTo(1);
        }
    }

    // =========================================================================
    // TargetPlayerRandomDiscardEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveTargetPlayerRandomDiscard")
    class ResolveTargetPlayerRandomDiscard {

        @Test
        @DisplayName("Target player discards at random with opponent flag")
        void targetDiscardsAtRandom() {
            Card card = createCard("Hypnotic Specter");
            TargetPlayerRandomDiscardEffect effect = new TargetPlayerRandomDiscardEffect(1, true);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
            gd.playerHands.get(player2Id).add(createCard("Mountain"));

            service.resolveTargetPlayerRandomDiscard(gd, entry, effect);

            assertThat(gd.discardCausedByOpponent).isTrue();
            assertThat(gd.playerHands.get(player2Id)).isEmpty();
            verify(graveyardService).addCardToGraveyard(eq(gd), eq(player2Id), any());
        }

        @Test
        @DisplayName("Controller discards when causedByOpponent is false")
        void controllerDiscardsWhenNotOpponent() {
            Card card = createCard("Wild Mongrel");
            TargetPlayerRandomDiscardEffect effect = new TargetPlayerRandomDiscardEffect(1, false);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.playerHands.get(player1Id).add(createCard("Mountain"));

            service.resolveTargetPlayerRandomDiscard(gd, entry, effect);

            assertThat(gd.discardCausedByOpponent).isFalse();
            assertThat(gd.playerHands.get(player1Id)).isEmpty();
        }

        @Test
        @DisplayName("No discard when hand is empty")
        void noDiscardWhenHandEmpty() {
            Card card = createCard("Hypnotic Specter");
            TargetPlayerRandomDiscardEffect effect = new TargetPlayerRandomDiscardEffect(1, true);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            service.resolveTargetPlayerRandomDiscard(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no cards to discard")));
        }
    }

    // =========================================================================
    // EachPlayerRandomDiscardEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveEachPlayerRandomDiscard")
    class ResolveEachPlayerRandomDiscard {

        @Test
        @DisplayName("Each player discards at random in APNAP order")
        void eachPlayerDiscardsAPNAP() {
            Card card = createCard("Burning Inquiry");
            EachPlayerRandomDiscardEffect effect = new EachPlayerRandomDiscardEffect(1);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.activePlayerId = player1Id;
            gd.playerHands.get(player1Id).add(createCard("Mountain"));
            gd.playerHands.get(player2Id).add(createCard("Forest"));

            service.resolveEachPlayerRandomDiscard(gd, entry, effect);

            // Both hands should have been reduced
            assertThat(gd.playerHands.get(player1Id)).isEmpty();
            assertThat(gd.playerHands.get(player2Id)).isEmpty();
            verify(graveyardService, times(2)).addCardToGraveyard(eq(gd), any(), any());
        }

        @Test
        @DisplayName("Controller's own discard is not opponent-caused")
        void controllerDiscardNotOpponentCaused() {
            Card card = createCard("Burning Inquiry");
            EachPlayerRandomDiscardEffect effect = new EachPlayerRandomDiscardEffect(1);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.activePlayerId = player1Id;
            gd.playerHands.get(player1Id).add(createCard("Mountain"));

            service.resolveEachPlayerRandomDiscard(gd, entry, effect);

            // After processing, the last discardCausedByOpponent set for p2 is true (opponent)
            // But p1 (active+controller) was processed first with false
            verify(graveyardService).addCardToGraveyard(eq(gd), eq(player1Id), any());
        }
    }

    // =========================================================================
    // SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveSacrificeSelfAndTargetDiscardsPerPoisonCounter")
    class ResolveSacrificeSelfAndTargetDiscardsPerPoisonCounter {

        @Test
        @DisplayName("Sacrifices source and target discards per poison counter")
        void sacrificesAndDiscardsPerPoison() {
            Card card = createCard("Flesh-Eater Imp");
            Permanent source = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(source);
            SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect effect = new SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect();
            StackEntry entry = createTriggeredEntryWithTarget(card, player1Id, List.of(effect), player2Id, source.getId());
            gd.playerPoisonCounters.put(player2Id, 3);
            gd.playerHands.get(player2Id).addAll(List.of(createCard("A"), createCard("B"), createCard("C")));

            when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);

            service.resolveSacrificeSelfAndTargetDiscardsPerPoisonCounter(gd, entry, effect);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, source);
            verify(playerInputService).beginDiscardChoice(gd, player2Id);
        }

        @Test
        @DisplayName("No discard when target has no poison counters")
        void noDiscardWhenNoPoisonCounters() {
            Card card = createCard("Flesh-Eater Imp");
            Permanent source = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(source);
            SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect effect = new SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect();
            StackEntry entry = createTriggeredEntryWithTarget(card, player1Id, List.of(effect), player2Id, source.getId());

            when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);

            service.resolveSacrificeSelfAndTargetDiscardsPerPoisonCounter(gd, entry, effect);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, source);
            verify(playerInputService, never()).beginDiscardChoice(any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no poison counters")));
        }

        @Test
        @DisplayName("Fizzles when source not found on battlefield")
        void fizzlesWhenSourceNotFound() {
            Card card = createCard("Flesh-Eater Imp");
            UUID fakePermanentId = UUID.randomUUID();
            SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect effect = new SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect();
            StackEntry entry = createTriggeredEntryWithTarget(card, player1Id, List.of(effect), player2Id, fakePermanentId);

            when(gameQueryService.findPermanentById(gd, fakePermanentId)).thenReturn(null);

            service.resolveSacrificeSelfAndTargetDiscardsPerPoisonCounter(gd, entry, effect);

            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("fizzles")));
        }

        @Test
        @DisplayName("Does nothing when target or source permanent is null")
        void doesNothingWhenNull() {
            Card card = createCard("Flesh-Eater Imp");
            SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect effect = new SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect();
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveSacrificeSelfAndTargetDiscardsPerPoisonCounter(gd, entry, effect);

            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
        }
    }

    // =========================================================================
    // TargetPlayerDiscardsReturnSelfIfCardTypeEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveTargetPlayerDiscardsReturnSelfIfCardType")
    class ResolveTargetPlayerDiscardsReturnSelfIfCardType {

        @Test
        @DisplayName("Sets pending return when target has cards")
        void setsPendingReturnWhenTargetHasCards() {
            Card card = createCard("Ravenous Rats");
            TargetPlayerDiscardsReturnSelfIfCardTypeEffect effect = new TargetPlayerDiscardsReturnSelfIfCardTypeEffect(1, CardType.CREATURE);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
            gd.playerHands.get(player2Id).add(createCard("Mountain"));

            service.resolveTargetPlayerDiscardsReturnSelfIfCardType(gd, entry, effect);

            assertThat(gd.pendingReturnToHandOnDiscardType).isNotNull();
            assertThat(gd.pendingReturnToHandOnDiscardType.requiredType()).isEqualTo(CardType.CREATURE);
            assertThat(gd.discardCausedByOpponent).isTrue();
        }

        @Test
        @DisplayName("Does not set pending return when target hand is empty")
        void noPendingReturnWhenHandEmpty() {
            Card card = createCard("Ravenous Rats");
            TargetPlayerDiscardsReturnSelfIfCardTypeEffect effect = new TargetPlayerDiscardsReturnSelfIfCardTypeEffect(1, CardType.CREATURE);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            service.resolveTargetPlayerDiscardsReturnSelfIfCardType(gd, entry, effect);

            assertThat(gd.pendingReturnToHandOnDiscardType).isNull();
        }
    }

    // =========================================================================
    // MayEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveMayEffect")
    class ResolveMayEffect {

        @Test
        @DisplayName("Sets resolvingMayEffectFromStack flag and adds pending may ability")
        void setsFlagAndAddsPendingMay() {
            Card card = createCard("Ob Nixilis");
            DrawCardEffect wrapped = new DrawCardEffect(1);
            MayEffect mayEffect = new MayEffect(wrapped, "Draw a card?");
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(mayEffect), player2Id);

            service.resolveMayEffect(gd, entry, mayEffect);

            assertThat(gd.resolvingMayEffectFromStack).isTrue();
            assertThat(gd.pendingMayAbilities).hasSize(1);
            assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player1Id);
            assertThat(gd.pendingMayAbilities.getFirst().effects()).containsExactly(wrapped);
        }

        @Test
        @DisplayName("Preserves target and source permanent IDs in pending may")
        void preservesTargetAndSourceIds() {
            Card card = createCard("Ob Nixilis");
            DrawCardEffect wrapped = new DrawCardEffect(1);
            MayEffect mayEffect = new MayEffect(wrapped, "Draw a card?");
            UUID sourcePermanentId = UUID.randomUUID();
            StackEntry entry = createTriggeredEntryWithTarget(card, player1Id, List.of(mayEffect), player2Id, sourcePermanentId);

            service.resolveMayEffect(gd, entry, mayEffect);

            assertThat(gd.pendingMayAbilities.getFirst().targetCardId()).isEqualTo(player2Id);
            assertThat(gd.pendingMayAbilities.getFirst().sourcePermanentId()).isEqualTo(sourcePermanentId);
        }
    }

    // =========================================================================
    // MayPayManaEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveMayPayManaEffect")
    class ResolveMayPayManaEffect {

        @Test
        @DisplayName("Sets resolvingMayEffectFromStack flag and adds pending may with mana cost")
        void setsFlagAndAddsPendingMayWithMana() {
            Card card = createCard("Rhystic Study");
            DrawCardEffect wrapped = new DrawCardEffect(1);
            MayPayManaEffect mayPayEffect = new MayPayManaEffect("{1}", wrapped, "Pay {1}?");
            StackEntry entry = createEntry(card, player1Id, List.of(mayPayEffect));

            service.resolveMayPayManaEffect(gd, entry, mayPayEffect);

            assertThat(gd.resolvingMayEffectFromStack).isTrue();
            assertThat(gd.pendingMayAbilities).hasSize(1);
            assertThat(gd.pendingMayAbilities.getFirst().manaCost()).isEqualTo("{1}");
            assertThat(gd.pendingMayAbilities.getFirst().effects()).containsExactly(wrapped);
        }
    }

    // =========================================================================
    // SacrificeUnlessDiscardCardTypeEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveSacrificeUnlessDiscardCardType")
    class ResolveSacrificeUnlessDiscardCardType {

        @Test
        @DisplayName("Sacrifices immediately when no valid cards in hand")
        void sacrificesWhenNoValidCards() {
            Card card = createCard("Zombie Infestation");
            Permanent source = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(source);
            SacrificeUnlessDiscardCardTypeEffect effect = new SacrificeUnlessDiscardCardTypeEffect(CardType.CREATURE);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            // Hand has no creature cards
            Card landCard = createCard("Mountain");
            landCard.setType(CardType.LAND);
            gd.playerHands.get(player1Id).add(landCard);

            service.resolveSacrificeUnlessDiscardCardType(gd, entry, effect);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, source);
        }

        @Test
        @DisplayName("Presents may ability when valid cards are available")
        void presentsMayWhenValidCardsAvailable() {
            Card card = createCard("Zombie Infestation");
            Permanent source = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(source);
            SacrificeUnlessDiscardCardTypeEffect effect = new SacrificeUnlessDiscardCardTypeEffect(CardType.CREATURE);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            Card creatureCard = createCard("Grizzly Bears");
            creatureCard.setType(CardType.CREATURE);
            gd.playerHands.get(player1Id).add(creatureCard);

            service.resolveSacrificeUnlessDiscardCardType(gd, entry, effect);

            assertThat(gd.pendingMayAbilities).isNotEmpty();
            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
        }

        @Test
        @DisplayName("Does nothing when source already left battlefield and no valid cards")
        void doesNothingWhenSourceGoneAndNoValidCards() {
            Card card = createCard("Zombie Infestation");
            SacrificeUnlessDiscardCardTypeEffect effect = new SacrificeUnlessDiscardCardTypeEffect(CardType.CREATURE);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveSacrificeUnlessDiscardCardType(gd, entry, effect);

            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
        }
    }

    // =========================================================================
    // SacrificeUnlessReturnOwnPermanentTypeToHandEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveSacrificeUnlessReturnOwnPermanentType")
    class ResolveSacrificeUnlessReturnOwnPermanentType {

        @Test
        @DisplayName("Sacrifices when no valid permanents on battlefield")
        void sacrificesWhenNoValidPermanents() {
            Card card = createCard("Cloud Spirit");
            Permanent source = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(source);
            SacrificeUnlessReturnOwnPermanentTypeToHandEffect effect = new SacrificeUnlessReturnOwnPermanentTypeToHandEffect(CardType.LAND);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveSacrificeUnlessReturnOwnPermanentType(gd, entry, effect);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, source);
        }

        @Test
        @DisplayName("Presents may ability when valid permanents exist")
        void presentsMayWhenValidPermanentsExist() {
            Card card = createCard("Cloud Spirit");
            Permanent source = new Permanent(card);
            Card landCard = createCard("Island");
            landCard.setType(CardType.LAND);
            Permanent landPerm = new Permanent(landCard);
            gd.playerBattlefields.get(player1Id).addAll(List.of(source, landPerm));
            SacrificeUnlessReturnOwnPermanentTypeToHandEffect effect = new SacrificeUnlessReturnOwnPermanentTypeToHandEffect(CardType.LAND);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveSacrificeUnlessReturnOwnPermanentType(gd, entry, effect);

            assertThat(gd.pendingMayAbilities).isNotEmpty();
            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
        }

        @Test
        @DisplayName("Does nothing when source gone and no valid permanents")
        void doesNothingWhenSourceGoneAndNoValidPermanents() {
            Card card = createCard("Cloud Spirit");
            SacrificeUnlessReturnOwnPermanentTypeToHandEffect effect = new SacrificeUnlessReturnOwnPermanentTypeToHandEffect(CardType.LAND);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveSacrificeUnlessReturnOwnPermanentType(gd, entry, effect);

            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
            assertThat(gd.pendingMayAbilities).isEmpty();
        }
    }

    // =========================================================================
    // RevealRandomCardFromTargetPlayerHandEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveRevealRandomCardFromTargetPlayerHand")
    class ResolveRevealRandomCardFromTargetPlayerHand {

        @Test
        @DisplayName("Reveals a random card from target's hand to all players")
        void revealsRandomCard() {
            Card card = createCard("Telepathy");
            RevealRandomCardFromTargetPlayerHandEffect effect = new RevealRandomCardFromTargetPlayerHandEffect();
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
            Card handCard = createCard("Mountain");
            gd.playerHands.get(player2Id).add(handCard);

            CardView mockView = mock(CardView.class);
            when(cardViewFactory.create(handCard)).thenReturn(mockView);

            service.resolveRevealRandomCardFromTargetPlayerHand(gd, entry);

            // All players receive the reveal message
            verify(sessionManager).sendToPlayer(eq(player1Id), any(RevealHandMessage.class));
            verify(sessionManager).sendToPlayer(eq(player2Id), any(RevealHandMessage.class));
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("reveals") && msg.contains("at random")));
        }

        @Test
        @DisplayName("Does nothing when target hand is empty")
        void doesNothingWhenHandEmpty() {
            Card card = createCard("Telepathy");
            RevealRandomCardFromTargetPlayerHandEffect effect = new RevealRandomCardFromTargetPlayerHandEffect();
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            service.resolveRevealRandomCardFromTargetPlayerHand(gd, entry);

            verify(sessionManager, never()).sendToPlayer(any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no cards to reveal")));
        }
    }

    // =========================================================================
    // OpponentMayPlayCreatureEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveOpponentMayPlayCreature")
    class ResolveOpponentMayPlayCreature {

        @Test
        @DisplayName("Logs when opponent has no creatures in hand")
        void logsWhenOpponentHasNoCreatures() {
            Card card = createCard("Hunted Wumpus");
            StackEntry entry = createEntry(card, player1Id, List.of());

            when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);

            service.resolveOpponentMayPlayCreature(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no creature cards in hand")));
        }

        @Test
        @DisplayName("Begins card choice when opponent has creatures")
        void beginsCardChoiceWhenCreaturesAvailable() {
            Card card = createCard("Hunted Wumpus");
            StackEntry entry = createEntry(card, player1Id, List.of());
            Card creatureCard = createCard("Grizzly Bears");
            creatureCard.setType(CardType.CREATURE);
            gd.playerHands.get(player2Id).add(creatureCard);

            when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);

            service.resolveOpponentMayPlayCreature(gd, entry);

            verify(playerInputService).beginCardChoice(eq(gd), eq(player2Id), any(), any());
        }
    }

    // =========================================================================
    // PutCardToBattlefieldEffect
    // =========================================================================

    @Nested
    @DisplayName("resolvePutCardToBattlefield")
    class ResolvePutCardToBattlefield {

        @Test
        @DisplayName("Presents card choice when matching cards exist in hand")
        void presentsChoiceWhenMatchingCards() {
            Card card = createCard("Elvish Piper");
            CardPredicate predicate = mock(CardPredicate.class);
            PutCardToBattlefieldEffect effect = new PutCardToBattlefieldEffect(predicate, "creature");
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            Card creatureCard = createCard("Grizzly Bears");
            gd.playerHands.get(player1Id).add(creatureCard);

            when(gameQueryService.matchesCardPredicate(eq(creatureCard), eq(predicate), any())).thenReturn(true);

            service.resolvePutCardToBattlefield(gd, entry, effect);

            verify(playerInputService).beginCardChoice(eq(gd), eq(player1Id), any(), any());
        }

        @Test
        @DisplayName("Logs and does nothing when no matching cards in hand")
        void noMatchingCards() {
            Card card = createCard("Elvish Piper");
            CardPredicate predicate = mock(CardPredicate.class);
            PutCardToBattlefieldEffect effect = new PutCardToBattlefieldEffect(predicate, "creature");
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            Card nonMatchingCard = createCard("Mountain");
            gd.playerHands.get(player1Id).add(nonMatchingCard);

            when(gameQueryService.matchesCardPredicate(eq(nonMatchingCard), eq(predicate), any())).thenReturn(false);

            service.resolvePutCardToBattlefield(gd, entry, effect);

            verify(playerInputService, never()).beginCardChoice(any(), any(), any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no creature cards in hand")));
        }

        @Test
        @DisplayName("Does nothing when hand is empty")
        void emptyHand() {
            Card card = createCard("Elvish Piper");
            CardPredicate predicate = mock(CardPredicate.class);
            PutCardToBattlefieldEffect effect = new PutCardToBattlefieldEffect(predicate, "creature");
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolvePutCardToBattlefield(gd, entry, effect);

            verify(playerInputService, never()).beginCardChoice(any(), any(), any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no creature cards in hand")));
        }
    }

    // =========================================================================
    // FlipCoinWinEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveFlipCoinWinEffect")
    class ResolveFlipCoinWinEffect {

        @Test
        @DisplayName("Always broadcasts flip result")
        void broadcastsFlipResult() {
            Card card = createCard("Krark's Thumb");
            DrawCardEffect wrapped = new DrawCardEffect(1);
            FlipCoinWinEffect effect = new FlipCoinWinEffect(wrapped);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            // Can't control ThreadLocalRandom, but we can verify the broadcast always happens
            service.resolveFlipCoinWinEffect(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("coin flip") && msg.contains("Player1")));
        }
    }

    // =========================================================================
    // FlipTwoCoinsEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveFlipTwoCoinsEffect")
    class ResolveFlipTwoCoinsEffect {

        @Test
        @DisplayName("Always broadcasts two coin flip results")
        void broadcastsTwoFlipResults() {
            Card card = createCard("Tavern Swindler");
            DrawCardEffect headsEffect = new DrawCardEffect(2);
            DiscardCardEffect tailsEffect = new DiscardCardEffect(1);
            FlipTwoCoinsEffect effect = new FlipTwoCoinsEffect(headsEffect, tailsEffect);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveFlipTwoCoinsEffect(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("flips two coins") && msg.contains("Player1")));
        }
    }

    // =========================================================================
    // DiscardUnlessExileCardFromGraveyardEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDiscardUnlessExileFromGraveyard")
    class ResolveDiscardUnlessExileFromGraveyard {

        @Test
        @DisplayName("Forces discard when no matching graveyard cards")
        void forcesDiscardWhenNoMatch() {
            Card card = createCard("Rotting Fensnake");
            CardPredicate predicate = mock(CardPredicate.class);
            DiscardUnlessExileCardFromGraveyardEffect effect = new DiscardUnlessExileCardFromGraveyardEffect(predicate);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.playerHands.get(player1Id).add(createCard("Mountain"));

            // No matching cards in graveyard
            service.resolveDiscardUnlessExileFromGraveyard(gd, entry, effect);

            assertThat(gd.discardCausedByOpponent).isFalse();
            verify(playerInputService).beginDiscardChoice(eq(gd), eq(player1Id));
        }

        @Test
        @DisplayName("Offers may ability when matching graveyard cards exist")
        void offersMayAbilityWhenMatchExists() {
            Card card = createCard("Rotting Fensnake");
            CardPredicate predicate = mock(CardPredicate.class);
            DiscardUnlessExileCardFromGraveyardEffect effect = new DiscardUnlessExileCardFromGraveyardEffect(predicate);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            Card graveyardCard = createCard("Zombie");
            gd.playerGraveyards.get(player1Id).add(graveyardCard);

            when(gameQueryService.matchesCardPredicate(eq(graveyardCard), eq(predicate), any())).thenReturn(true);

            service.resolveDiscardUnlessExileFromGraveyard(gd, entry, effect);

            assertThat(gd.pendingMayAbilities).isNotEmpty();
            assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player1Id);
        }
    }

    // =========================================================================
    // DiscardUpToThenDrawThatManyEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDiscardUpToThenDraw")
    class ResolveDiscardUpToThenDraw {

        @Test
        @DisplayName("Begins X value choice when hand is not empty")
        void beginsXValueChoice() {
            Card card = createCard("Faithful Mending");
            DiscardUpToThenDrawThatManyEffect effect = new DiscardUpToThenDrawThatManyEffect(3);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.playerHands.get(player1Id).addAll(List.of(createCard("A"), createCard("B")));

            service.resolveDiscardUpToThenDraw(gd, entry, effect);

            verify(playerInputService).beginXValueChoice(eq(gd), eq(player1Id), eq(2), any(), any());
        }

        @Test
        @DisplayName("Does nothing when hand is empty")
        void emptyHand() {
            Card card = createCard("Faithful Mending");
            DiscardUpToThenDrawThatManyEffect effect = new DiscardUpToThenDrawThatManyEffect(3);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveDiscardUpToThenDraw(gd, entry, effect);

            verify(playerInputService, never()).beginXValueChoice(any(), any(), any(int.class), any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no cards to discard")));
        }

        @Test
        @DisplayName("On re-entry with chosenXValue, sets up discard")
        void reEntryWithChosenXValue() {
            Card card = createCard("Faithful Mending");
            DiscardUpToThenDrawThatManyEffect effect = new DiscardUpToThenDrawThatManyEffect(3);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.playerHands.get(player1Id).addAll(List.of(createCard("A"), createCard("B")));
            gd.chosenXValue = 2;

            service.resolveDiscardUpToThenDraw(gd, entry, effect);

            assertThat(gd.pendingRummageDrawCount).isEqualTo(2);
            assertThat(gd.chosenXValue).isNull();
            verify(playerInputService).beginDiscardChoice(eq(gd), eq(player1Id));
        }

        @Test
        @DisplayName("On re-entry with chosenXValue of 0, does nothing")
        void reEntryWithZeroChosen() {
            Card card = createCard("Faithful Mending");
            DiscardUpToThenDrawThatManyEffect effect = new DiscardUpToThenDrawThatManyEffect(3);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.chosenXValue = 0;

            service.resolveDiscardUpToThenDraw(gd, entry, effect);

            assertThat(gd.chosenXValue).isNull();
            verify(playerInputService, never()).beginDiscardChoice(any(), any());
        }
    }

    // =========================================================================
    // TargetPlayerExilesFromHandEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveTargetPlayerExilesFromHand")
    class ResolveTargetPlayerExilesFromHand {

        @Test
        @DisplayName("Begins exile from hand choice when target has cards")
        void beginsExileChoice() {
            Card card = createCard("Sin Collector");
            TargetPlayerExilesFromHandEffect effect = new TargetPlayerExilesFromHandEffect(1);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
            gd.playerHands.get(player2Id).add(createCard("Lightning Bolt"));

            Permanent sourcePermanent = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(sourcePermanent);

            service.resolveTargetPlayerExilesFromHand(gd, entry, effect);

            verify(playerInputService).beginExileFromHandChoice(eq(gd), eq(player2Id), any());
        }

        @Test
        @DisplayName("Logs and does nothing when target hand is empty")
        void emptyTargetHand() {
            Card card = createCard("Sin Collector");
            TargetPlayerExilesFromHandEffect effect = new TargetPlayerExilesFromHandEffect(1);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            service.resolveTargetPlayerExilesFromHand(gd, entry, effect);

            verify(playerInputService, never()).beginExileFromHandChoice(any(), any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no cards to exile")));
        }
    }

    // =========================================================================
    // ChooseCardFromTargetHandToDiscardEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveChooseCardFromTargetHandToDiscardHandler")
    class ResolveChooseCardFromTargetHandToDiscard {

        @Test
        @DisplayName("Reveals hand and begins choice when target has valid cards")
        void revealsHandAndBeginsChoice() {
            Card card = createCard("Thoughtseize");
            ChooseCardFromTargetHandToDiscardEffect effect = new ChooseCardFromTargetHandToDiscardEffect(1, List.of(CardType.LAND));
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
            Card targetCard = createCard("Lightning Bolt");
            targetCard.setType(CardType.INSTANT);
            gd.playerHands.get(player2Id).add(targetCard);

            service.resolveChooseCardFromTargetHandToDiscardHandler(gd, entry, effect);

            assertThat(gd.discardCausedByOpponent).isTrue();
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("reveals their hand")));
            verify(playerInputService).beginRevealedHandChoice(eq(gd), eq(player1Id), eq(player2Id), any(), any());
        }

        @Test
        @DisplayName("Logs empty hand when target has no cards")
        void emptyHand() {
            Card card = createCard("Thoughtseize");
            ChooseCardFromTargetHandToDiscardEffect effect = new ChooseCardFromTargetHandToDiscardEffect(1, List.of(CardType.LAND));
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            service.resolveChooseCardFromTargetHandToDiscardHandler(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("hand") && msg.contains("empty")));
        }
    }

    // =========================================================================
    // ChooseCardFromTargetHandToExileEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveChooseCardFromTargetHandToExileHandler")
    class ResolveChooseCardFromTargetHandToExile {

        @Test
        @DisplayName("Reveals hand and begins exile choice")
        void revealsHandAndBeginsExile() {
            Card card = createCard("Tidehollow Sculler");
            ChooseCardFromTargetHandToExileEffect effect = new ChooseCardFromTargetHandToExileEffect(1, List.of(CardType.LAND));
            UUID sourcePermanentId = UUID.randomUUID();
            StackEntry entry = createTriggeredEntryWithTarget(card, player1Id, List.of(effect), player2Id, sourcePermanentId);
            Card targetCard = createCard("Lightning Bolt");
            targetCard.setType(CardType.INSTANT);
            gd.playerHands.get(player2Id).add(targetCard);

            service.resolveChooseCardFromTargetHandToExileHandler(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("reveals their hand")));
            verify(playerInputService).beginRevealedHandChoice(eq(gd), eq(player1Id), eq(player2Id), any(), any());
        }

        @Test
        @DisplayName("Logs empty hand when target has no cards")
        void emptyHand() {
            Card card = createCard("Tidehollow Sculler");
            ChooseCardFromTargetHandToExileEffect effect = new ChooseCardFromTargetHandToExileEffect(1, List.of(CardType.LAND));
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            service.resolveChooseCardFromTargetHandToExileHandler(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("hand") && msg.contains("empty")));
        }
    }

    // =========================================================================
    // ChooseCardNameAndExileFromZonesEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveChooseCardNameAndExileFromZones")
    class ResolveChooseCardNameAndExileFromZones {

        @Test
        @DisplayName("Begins spell card name choice for controller")
        void beginsSpellCardNameChoice() {
            Card card = createCard("Slaughter Games");
            ChooseCardNameAndExileFromZonesEffect effect = new ChooseCardNameAndExileFromZonesEffect(List.of(CardType.LAND));
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            service.resolveChooseCardNameAndExileFromZones(gd, entry, effect);

            verify(playerInputService).beginSpellCardNameChoice(gd, player1Id, player2Id, List.of(CardType.LAND));
        }
    }

    // =========================================================================
    // ExileTargetGraveyardCardAndSameNameFromZonesEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveExileTargetGraveyardCardAndSameNameFromZones")
    class ResolveExileTargetGraveyardCardAndSameNameFromZones {

        @Test
        @DisplayName("Does nothing when target card not found in graveyard")
        void fizzlesWhenTargetGone() {
            Card card = createCard("Surgical Extraction");
            ExileTargetGraveyardCardAndSameNameFromZonesEffect effect = new ExileTargetGraveyardCardAndSameNameFromZonesEffect();
            UUID targetCardId = UUID.randomUUID();
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), targetCardId);

            when(gameQueryService.findCardInGraveyardById(gd, targetCardId)).thenReturn(null);

            service.resolveExileTargetGraveyardCardAndSameNameFromZones(gd, entry);

            verify(playerInputService, never()).beginMultiZoneExileChoice(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Finds matching cards across zones and begins multi-zone exile choice")
        void findsMatchingCardsAcrossZones() {
            Card card = createCard("Surgical Extraction");
            ExileTargetGraveyardCardAndSameNameFromZonesEffect effect = new ExileTargetGraveyardCardAndSameNameFromZonesEffect();

            Card targetCard = createCard("Lightning Bolt");
            UUID targetCardId = targetCard.getId();
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), targetCardId);

            Card handCopy = createCard("Lightning Bolt");
            gd.playerHands.get(player2Id).add(handCopy);
            Card graveyardCopy = createCard("Lightning Bolt");
            gd.playerGraveyards.get(player2Id).add(graveyardCopy);

            when(gameQueryService.findCardInGraveyardById(gd, targetCardId)).thenReturn(targetCard);
            when(gameQueryService.findGraveyardOwnerById(gd, targetCardId)).thenReturn(player2Id);

            service.resolveExileTargetGraveyardCardAndSameNameFromZones(gd, entry);

            verify(playerInputService).beginMultiZoneExileChoice(eq(gd), eq(player1Id), any(), eq(player2Id), eq("Lightning Bolt"));
        }

        @Test
        @DisplayName("Shuffles library and logs when no matching cards found")
        void noMatchingCards() {
            Card card = createCard("Surgical Extraction");
            ExileTargetGraveyardCardAndSameNameFromZonesEffect effect = new ExileTargetGraveyardCardAndSameNameFromZonesEffect();

            Card targetCard = createCard("Unique Spell");
            UUID targetCardId = targetCard.getId();
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), targetCardId);

            when(gameQueryService.findCardInGraveyardById(gd, targetCardId)).thenReturn(targetCard);
            when(gameQueryService.findGraveyardOwnerById(gd, targetCardId)).thenReturn(player2Id);
            // No matching cards anywhere

            service.resolveExileTargetGraveyardCardAndSameNameFromZones(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("exiles 0 cards") && msg.contains("Unique Spell")));
        }
    }

    // =========================================================================
    // DrawAndRandomDiscardWithSharedTypeCountersEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDrawAndRandomDiscardWithSharedTypeCounters")
    class ResolveDrawAndRandomDiscardWithSharedTypeCounters {

        @Test
        @DisplayName("Draws cards and discards at random")
        void drawsAndDiscardsAtRandom() {
            Card card = createCard("Wild Mongrel");
            DrawAndRandomDiscardWithSharedTypeCountersEffect effect =
                    new DrawAndRandomDiscardWithSharedTypeCountersEffect(2, 2, 1);
            UUID sourcePermanentId = UUID.randomUUID();
            StackEntry entry = createTriggeredEntry(card, player1Id, List.of(effect), sourcePermanentId);
            gd.playerHands.get(player1Id).addAll(List.of(createCard("A"), createCard("B"), createCard("C")));

            service.resolveDrawAndRandomDiscardWithSharedTypeCounters(gd, entry, effect);

            verify(drawService, times(2)).resolveDrawCard(gd, player1Id);
            verify(graveyardService, times(2)).addCardToGraveyard(eq(gd), eq(player1Id), any());
        }

        @Test
        @DisplayName("Sets discardCausedByOpponent to false")
        void setsDiscardNotByOpponent() {
            Card card = createCard("Wild Mongrel");
            DrawAndRandomDiscardWithSharedTypeCountersEffect effect =
                    new DrawAndRandomDiscardWithSharedTypeCountersEffect(1, 1, 1);
            UUID sourcePermanentId = UUID.randomUUID();
            StackEntry entry = createTriggeredEntry(card, player1Id, List.of(effect), sourcePermanentId);
            gd.playerHands.get(player1Id).add(createCard("A"));

            service.resolveDrawAndRandomDiscardWithSharedTypeCounters(gd, entry, effect);

            assertThat(gd.discardCausedByOpponent).isFalse();
        }
    }

    // =========================================================================
    // ReturnPermanentsOnCombatDamageToPlayerEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveReturnPermanentsOnCombatDamage")
    class ResolveReturnPermanentsOnCombatDamage {

        @Test
        @DisplayName("Begins multi-permanent choice when defender has valid permanents")
        void beginsMultiPermanentChoice() {
            Card card = createCard("Ninja of the Deep Hours");
            ReturnPermanentsOnCombatDamageToPlayerEffect effect = new ReturnPermanentsOnCombatDamageToPlayerEffect();
            // controllerId=attacker, targetId=defender, xValue=damage dealt
            StackEntry entry = createEntryWithXValueAndTarget(card, player1Id, List.of(effect), 3, player2Id);

            Permanent perm1 = new Permanent(createCard("Grizzly Bears"));
            Permanent perm2 = new Permanent(createCard("Hill Giant"));
            gd.playerBattlefields.get(player2Id).add(perm1);
            gd.playerBattlefields.get(player2Id).add(perm2);

            service.resolveReturnPermanentsOnCombatDamage(gd, entry, effect);

            assertThat(gd.pendingCombatDamageBounceTargetPlayerId).isEqualTo(player2Id);
            verify(playerInputService).beginMultiPermanentChoice(eq(gd), eq(player1Id), any(), eq(2), any());
        }

        @Test
        @DisplayName("Logs and does nothing when defender has no permanents")
        void noDefenderPermanents() {
            Card card = createCard("Ninja of the Deep Hours");
            ReturnPermanentsOnCombatDamageToPlayerEffect effect = new ReturnPermanentsOnCombatDamageToPlayerEffect();
            StackEntry entry = createEntryWithXValueAndTarget(card, player1Id, List.of(effect), 2, player2Id);

            service.resolveReturnPermanentsOnCombatDamage(gd, entry, effect);

            verify(playerInputService, never()).beginMultiPermanentChoice(any(), any(), any(), any(int.class), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no permanents")));
        }

        @Test
        @DisplayName("Filters by predicate when filter is set")
        void filtersByPredicate() {
            Card card = createCard("Scalpelexis");
            PermanentPredicate filter = mock(PermanentPredicate.class);
            ReturnPermanentsOnCombatDamageToPlayerEffect effect = new ReturnPermanentsOnCombatDamageToPlayerEffect(filter);
            StackEntry entry = createEntryWithXValueAndTarget(card, player1Id, List.of(effect), 2, player2Id);

            Permanent matching = new Permanent(createCard("Grizzly Bears"));
            Permanent nonMatching = new Permanent(createCard("Mountain"));
            gd.playerBattlefields.get(player2Id).add(matching);
            gd.playerBattlefields.get(player2Id).add(nonMatching);

            when(gameQueryService.matchesPermanentPredicate(gd, matching, filter)).thenReturn(true);
            when(gameQueryService.matchesPermanentPredicate(gd, nonMatching, filter)).thenReturn(false);

            service.resolveReturnPermanentsOnCombatDamage(gd, entry, effect);

            verify(playerInputService).beginMultiPermanentChoice(eq(gd), eq(player1Id),
                    argThat(ids -> ids.size() == 1 && ids.contains(matching.getId())), eq(1), any());
        }
    }

    // =========================================================================
    // PutAwakeningCountersOnTargetLandsEffect
    // =========================================================================

    @Nested
    @DisplayName("resolvePutAwakeningCounters")
    class ResolvePutAwakeningCounters {

        @Test
        @DisplayName("Begins multi-permanent choice when controller has lands")
        void beginsChoiceWithLands() {
            Card card = createCard("Embodiment of Insight");
            PutAwakeningCountersOnTargetLandsEffect effect = new PutAwakeningCountersOnTargetLandsEffect();
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            Card landCard = createCard("Forest");
            landCard.setType(CardType.LAND);
            Permanent land = new Permanent(landCard);
            gd.playerBattlefields.get(player1Id).add(land);

            service.resolvePutAwakeningCounters(gd, entry);

            assertThat(gd.pendingAwakeningCounterPlacement).isTrue();
            verify(playerInputService).beginMultiPermanentChoice(eq(gd), eq(player1Id),
                    argThat(ids -> ids.size() == 1 && ids.contains(land.getId())), eq(1), any());
        }

        @Test
        @DisplayName("Logs and does nothing when controller has no lands")
        void noLands() {
            Card card = createCard("Embodiment of Insight");
            PutAwakeningCountersOnTargetLandsEffect effect = new PutAwakeningCountersOnTargetLandsEffect();
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            // Only a non-land on battlefield
            Card creatureCard = createCard("Grizzly Bears");
            creatureCard.setType(CardType.CREATURE);
            Permanent creature = new Permanent(creatureCard);
            gd.playerBattlefields.get(player1Id).add(creature);

            service.resolvePutAwakeningCounters(gd, entry);

            verify(playerInputService, never()).beginMultiPermanentChoice(any(), any(), any(), any(int.class), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no lands")));
        }
    }

    // =========================================================================
    // SacrificeArtifactThenDealDividedDamageEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveSacrificeArtifactThenDealDividedDamage")
    class ResolveSacrificeArtifactThenDealDividedDamage {

        @Test
        @DisplayName("Begins permanent choice when controller has artifacts")
        void beginsChoiceWithArtifacts() {
            Card card = createCard("Shrapnel Blast");
            SacrificeArtifactThenDealDividedDamageEffect effect = new SacrificeArtifactThenDealDividedDamageEffect(5);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.pendingETBDamageAssignments = new HashMap<>(Map.of(player2Id, 5));

            Permanent artifact = new Permanent(createCard("Sol Ring"));
            gd.playerBattlefields.get(player1Id).add(artifact);

            when(gameQueryService.isArtifact(artifact)).thenReturn(true);

            service.resolveSacrificeArtifactThenDealDividedDamage(gd, entry, effect);

            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id), any(), any());
        }

        @Test
        @DisplayName("Logs and clears assignments when no artifacts available")
        void noArtifacts() {
            Card card = createCard("Shrapnel Blast");
            SacrificeArtifactThenDealDividedDamageEffect effect = new SacrificeArtifactThenDealDividedDamageEffect(5);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.pendingETBDamageAssignments = new HashMap<>(Map.of(player2Id, 5));

            service.resolveSacrificeArtifactThenDealDividedDamage(gd, entry, effect);

            verify(playerInputService, never()).beginPermanentChoice(any(), any(), any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no artifacts")));
            assertThat(gd.pendingETBDamageAssignments).isEmpty();
        }
    }

    // =========================================================================
    // SacrificePermanentThenEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveSacrificePermanentThen")
    class ResolveSacrificePermanentThen {

        @Test
        @DisplayName("Begins permanent choice when matching permanents exist")
        void beginsChoiceWithMatching() {
            Card card = createCard("Goblin Bombardment");
            PermanentPredicate filter = mock(PermanentPredicate.class);
            DrawCardEffect thenEffect = new DrawCardEffect(1);
            SacrificePermanentThenEffect effect = new SacrificePermanentThenEffect(filter, thenEffect, "a creature");
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            Permanent creature = new Permanent(createCard("Grizzly Bears"));
            gd.playerBattlefields.get(player1Id).add(creature);

            when(gameQueryService.matchesPermanentPredicate(gd, creature, filter)).thenReturn(true);

            service.resolveSacrificePermanentThen(gd, entry, effect);

            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id),
                    argThat(ids -> ids.contains(creature.getId())), any());
        }

        @Test
        @DisplayName("Logs and does nothing when no matching permanents")
        void noMatching() {
            Card card = createCard("Goblin Bombardment");
            PermanentPredicate filter = mock(PermanentPredicate.class);
            DrawCardEffect thenEffect = new DrawCardEffect(1);
            SacrificePermanentThenEffect effect = new SacrificePermanentThenEffect(filter, thenEffect, "a creature");
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            Permanent nonMatching = new Permanent(createCard("Mountain"));
            gd.playerBattlefields.get(player1Id).add(nonMatching);

            when(gameQueryService.matchesPermanentPredicate(gd, nonMatching, filter)).thenReturn(false);

            service.resolveSacrificePermanentThen(gd, entry, effect);

            verify(playerInputService, never()).beginPermanentChoice(any(), any(), any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no a creature to sacrifice")));
        }
    }

    // =========================================================================
    // ChooseCardsFromTargetHandToTopOfLibraryEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveChooseCardsFromTargetHandToTopOfLibrary")
    class ResolveChooseCardsFromTargetHandToTopOfLibrary {

        @Test
        @DisplayName("Reveals hand and begins choice when target has cards")
        void revealsAndBeginsChoice() {
            Card card = createCard("Lapse of Certainty");
            ChooseCardsFromTargetHandToTopOfLibraryEffect effect = new ChooseCardsFromTargetHandToTopOfLibraryEffect(1);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
            gd.playerHands.get(player2Id).addAll(List.of(createCard("A"), createCard("B")));

            service.resolveChooseCardsFromTargetHandToTopOfLibrary(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("looks at") && msg.contains("Player2")));
            verify(playerInputService).beginRevealedHandChoice(eq(gd), eq(player1Id), eq(player2Id), any(), any());
        }

        @Test
        @DisplayName("Logs empty hand when target has no cards")
        void emptyHand() {
            Card card = createCard("Lapse of Certainty");
            ChooseCardsFromTargetHandToTopOfLibraryEffect effect = new ChooseCardsFromTargetHandToTopOfLibraryEffect(1);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            service.resolveChooseCardsFromTargetHandToTopOfLibrary(gd, entry, effect);

            verify(playerInputService, never()).beginRevealedHandChoice(any(), any(), any(), any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("empty")));
        }
    }

    // =========================================================================
    // ChangeColorTextEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveChangeColorText")
    class ResolveChangeColorText {

        @Test
        @DisplayName("Begins color choice when target permanent exists")
        void beginsColorChoice() {
            Card card = createCard("Trait Doctoring");
            ChangeColorTextEffect effect = new ChangeColorTextEffect();
            Permanent target = new Permanent(createCard("Grizzly Bears"));
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);

            service.resolveChangeColorText(gd, entry);

            verify(sessionManager).sendToPlayer(eq(player1Id), any(ChooseFromListMessage.class));
        }

        @Test
        @DisplayName("Does nothing when target permanent is gone")
        void targetGone() {
            Card card = createCard("Trait Doctoring");
            ChangeColorTextEffect effect = new ChangeColorTextEffect();
            UUID missingId = UUID.randomUUID();
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), missingId);

            when(gameQueryService.findPermanentById(gd, missingId)).thenReturn(null);

            service.resolveChangeColorText(gd, entry);

            verify(sessionManager, never()).sendToPlayer(any(), any(ChooseFromListMessage.class));
        }
    }

    // =========================================================================
    // AwardAnyColorManaWithInstantSorceryCopyEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveAwardAnyColorManaWithCopy")
    class ResolveAwardAnyColorManaWithCopy {

        @Test
        @DisplayName("Sends color choice and registers spell copy trigger")
        void sendsColorChoiceAndRegistersCopy() {
            Card card = createCard("Unexpected Windfall");
            AwardAnyColorManaWithInstantSorceryCopyEffect effect = new AwardAnyColorManaWithInstantSorceryCopyEffect(2);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveAwardAnyColorManaWithCopy(gd, entry, effect);

            verify(sessionManager).sendToPlayer(eq(player1Id), any(ChooseFromListMessage.class));
            assertThat(gd.pendingNextInstantSorceryCopyCount.get(player1Id)).isEqualTo(1);
        }
    }

    // =========================================================================
    // AwardAnyColorManaEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveAwardAnyColorMana")
    class ResolveAwardAnyColorMana {

        @Test
        @DisplayName("Sends color choice to controller")
        void sendsColorChoice() {
            Card card = createCard("Birds of Paradise");
            AwardAnyColorManaEffect effect = new AwardAnyColorManaEffect(1);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            service.resolveAwardAnyColorMana(gd, entry, effect);

            verify(sessionManager).sendToPlayer(eq(player1Id), any(ChooseFromListMessage.class));
        }
    }

    // =========================================================================
    // AddManaPerAttackingCreatureEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveAddManaPerAttackingCreature")
    class ResolveAddManaPerAttackingCreature {

        @Test
        @DisplayName("Sends color choice when attackers exist")
        void sendsColorChoiceWithAttackers() {
            Card card = createCard("Druids' Repository");
            AddManaPerAttackingCreatureEffect effect = new AddManaPerAttackingCreatureEffect(ManaColor.RED, ManaColor.GREEN);
            // xValue = attacker count at trigger time
            StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 3);

            service.resolveAddManaPerAttackingCreature(gd, entry, effect);

            verify(sessionManager).sendToPlayer(eq(player1Id), any(ChooseFromListMessage.class));
        }

        @Test
        @DisplayName("Does nothing when xValue (attacker count) is 0")
        void doesNothingWithZeroAttackers() {
            Card card = createCard("Druids' Repository");
            AddManaPerAttackingCreatureEffect effect = new AddManaPerAttackingCreatureEffect(ManaColor.RED, ManaColor.GREEN);
            StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 0);

            service.resolveAddManaPerAttackingCreature(gd, entry, effect);

            verify(sessionManager, never()).sendToPlayer(any(), any(ChooseFromListMessage.class));
        }
    }

    // =========================================================================
    // RevealRandomHandCardAndPlayEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveRevealRandomHandCardAndPlay")
    class ResolveRevealRandomHandCardAndPlay {

        @Test
        @DisplayName("Logs when target hand is empty")
        void emptyHand() {
            Card card = createCard("Wild Evocation");
            RevealRandomHandCardAndPlayEffect effect = new RevealRandomHandCardAndPlayEffect();
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            service.resolveRevealRandomHandCardAndPlay(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no cards in hand")));
        }

        @Test
        @DisplayName("Puts land directly onto the battlefield")
        void putsLandOntoBattlefield() {
            Card card = createCard("Wild Evocation");
            RevealRandomHandCardAndPlayEffect effect = new RevealRandomHandCardAndPlayEffect();
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            Card landCard = createCard("Forest");
            landCard.setType(CardType.LAND);
            gd.playerHands.get(player2Id).add(landCard);
            CardView mockView = mock(CardView.class);
            when(cardViewFactory.create(landCard)).thenReturn(mockView);

            service.resolveRevealRandomHandCardAndPlay(gd, entry);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(player2Id), any(Permanent.class));
            assertThat(gd.playerHands.get(player2Id)).isEmpty();
        }

        @Test
        @DisplayName("Reveals card to all players")
        void revealsToAllPlayers() {
            Card card = createCard("Wild Evocation");
            RevealRandomHandCardAndPlayEffect effect = new RevealRandomHandCardAndPlayEffect();
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            Card revealedCard = createCard("Mountain");
            revealedCard.setType(CardType.LAND);
            gd.playerHands.get(player2Id).add(revealedCard);
            CardView mockView = mock(CardView.class);
            when(cardViewFactory.create(revealedCard)).thenReturn(mockView);

            service.resolveRevealRandomHandCardAndPlay(gd, entry);

            verify(sessionManager).sendToPlayer(eq(player1Id), any(RevealHandMessage.class));
            verify(sessionManager).sendToPlayer(eq(player2Id), any(RevealHandMessage.class));
        }
    }
}
