package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFromHandToImprintEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileSupport;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

@ExtendWith(MockitoExtension.class)
class ExileFromHandToImprintEffectHandlerTest {

    @Mock private GraveyardService graveyardService;
    @Mock private GameQueryService gameQueryService;
    @Mock private PredicateEvaluationService predicateEvaluationService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private PermanentRemovalService permanentRemovalService;
    @Mock private PlayerInputService playerInputService;
    @Mock private CardViewFactory cardViewFactory;
    @Mock private TriggerCollectionService triggerCollectionService;
    @Mock private BattlefieldEntryService battlefieldEntryService;
    @Mock private ExileService exileService;
    @InjectMocks
    private ExileSupport exileSupport;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private ExileFromHandToImprintEffectHandler exileFromHandToImprintHandler;

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
        exileFromHandToImprintHandler = new ExileFromHandToImprintEffectHandler(gameQueryService, predicateEvaluationService, playerInputService);

    }

    // ===== Helper methods =====

        private Card createCard(String name) {
            Card card = new Card();
            card.setName(name);
            card.setType(CardType.ARTIFACT);
            return card;
        }

        private Card createCreatureCard(String name) {
            Card card = new Card();
            card.setName(name);
            card.setType(CardType.CREATURE);
            card.setPower(2);
            card.setToughness(2);
            return card;
        }

        private Card createSorceryCard(String name) {
            Card card = new Card();
            card.setName(name);
            card.setType(CardType.SORCERY);
            return card;
        }

        private Card createLandCard(String name) {
            Card card = new Card();
            card.setName(name);
            card.setType(CardType.LAND);
            return card;
        }

        private Permanent addPermanent(UUID playerId, Card card) {
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            gd.playerBattlefields.get(playerId).add(perm);
            return perm;
        }

        private StackEntry createSingleTargetEntry(Card sourceCard, UUID controllerId, UUID targetId) {
            return new StackEntry(
                    StackEntryType.SORCERY_SPELL, sourceCard, controllerId, sourceCard.getName(),
                    List.of(new ExileTargetPermanentEffect()), 0, targetId, null
            );
        }

        private StackEntry createMultiTargetEntry(Card sourceCard, UUID controllerId, List<UUID> targetIds) {
            return new StackEntry(
                    StackEntryType.INSTANT_SPELL, sourceCard, controllerId, sourceCard.getName(),
                    List.of(new ExileTargetPermanentEffect()), 0, targetIds
            );
        }

        /** Makes the mocked exileService actually add cards to exile so assertions on GameData work. */
        private void stubExileCardTrackedWithSource() {
            doAnswer(inv -> {
                GameData gameData = inv.getArgument(0);
                UUID ownerId = inv.getArgument(1);
                Card card = inv.getArgument(2);
                UUID sourcePermanentId = inv.getArgument(3);
                gameData.addToExile(ownerId, card, sourcePermanentId);
                return null;
            }).when(exileService).exileCard(any(), any(), any(), any());
        }

        // =========================================================================
        // ExileAllPermanentsEffect
        // =========================================================================

    private final CardPredicate filter = new CardNotPredicate(new CardTypePredicate(CardType.LAND));

            @Test
            @DisplayName("Prompts player to choose a card from hand to imprint")
            void promptsCardChoice() {
                Card anvilCard = createCard("Semblance Anvil");
                Permanent anvilPerm = new Permanent(anvilCard);

                gd.playerHands.get(player1Id).clear();
                Card handCard = createCreatureCard("Grizzly Bears");
                gd.playerHands.get(player1Id).add(handCard);

                ExileFromHandToImprintEffect effect = new ExileFromHandToImprintEffect(filter, "a nonland card");
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, anvilCard, player1Id, "Semblance Anvil trigger",
                        List.of(effect), anvilPerm.getId(), (UUID) null
                );

                when(gameQueryService.findPermanentById(gd, anvilPerm.getId())).thenReturn(anvilPerm);
                when(predicateEvaluationService.matchesCardPredicate(eq(handCard), any(), any())).thenReturn(true);

                exileFromHandToImprintHandler.resolve(gd, entry, effect);

                verify(playerInputService).beginImprintFromHandChoice(
                        eq(gd), eq(player1Id), eq(List.of(0)),
                        eq("Choose a nonland card from your hand to exile and imprint."),
                        eq(anvilPerm.getId()));
            }

            @Test
            @DisplayName("Skips when controller has no matching cards in hand")
            void skipsWhenNoMatchingCards() {
                Card anvilCard = createCard("Semblance Anvil");
                Permanent anvilPerm = new Permanent(anvilCard);

                gd.playerHands.get(player1Id).clear();

                ExileFromHandToImprintEffect effect = new ExileFromHandToImprintEffect(filter, "a nonland card");
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, anvilCard, player1Id, "Semblance Anvil trigger",
                        List.of(effect), anvilPerm.getId(), (UUID) null
                );

                when(gameQueryService.findPermanentById(gd, anvilPerm.getId())).thenReturn(anvilPerm);

                exileFromHandToImprintHandler.resolve(gd, entry, effect);

                verify(playerInputService, never()).beginImprintFromHandChoice(any(), any(), any(), any(), any());
            }

            @Test
            @DisplayName("Skips when no cards match the filter")
            void skipsWhenNoCardsMatchFilter() {
                Card anvilCard = createCard("Semblance Anvil");
                Permanent anvilPerm = new Permanent(anvilCard);

                gd.playerHands.get(player1Id).clear();
                Card handCard = createCreatureCard("Grizzly Bears");
                gd.playerHands.get(player1Id).add(handCard);

                ExileFromHandToImprintEffect effect = new ExileFromHandToImprintEffect(filter, "a nonland card");
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, anvilCard, player1Id, "Semblance Anvil trigger",
                        List.of(effect), anvilPerm.getId(), (UUID) null
                );

                when(gameQueryService.findPermanentById(gd, anvilPerm.getId())).thenReturn(anvilPerm);
                when(predicateEvaluationService.matchesCardPredicate(eq(handCard), any(), any())).thenReturn(false);

                exileFromHandToImprintHandler.resolve(gd, entry, effect);

                verify(playerInputService, never()).beginImprintFromHandChoice(any(), any(), any(), any(), any());
            }

            @Test
            @DisplayName("Does nothing when source permanent is gone")
            void fizzlesWhenSourceGone() {
                UUID anvilId = UUID.randomUUID();
                Card anvilCard = createCard("Semblance Anvil");

                ExileFromHandToImprintEffect effect = new ExileFromHandToImprintEffect(filter, "a nonland card");
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, anvilCard, player1Id, "Semblance Anvil trigger",
                        List.of(effect), anvilId, (UUID) null
                );

                when(gameQueryService.findPermanentById(gd, anvilId)).thenReturn(null);

                exileFromHandToImprintHandler.resolve(gd, entry, effect);

                verify(playerInputService, never()).beginImprintFromHandChoice(any(), any(), any(), any(), any());
            }
}
