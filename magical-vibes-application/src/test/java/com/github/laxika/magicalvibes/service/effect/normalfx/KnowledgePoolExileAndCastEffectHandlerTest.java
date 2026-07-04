package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingKnowledgePoolCast;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolExileAndCastEffect;
import com.github.laxika.magicalvibes.model.effect.MillHalfLibraryEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.interaction.KnowledgePoolCastChoiceInteractionHandler;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.KnowledgePoolExileAndCastEffectHandler;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgePoolExileAndCastEffectHandlerTest {

    @Mock private GraveyardService graveyardService;
    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private PermanentRemovalService permanentRemovalService;
    @Mock private PlayerInputService playerInputService;
    @Mock private CardViewFactory cardViewFactory;
    @Mock private SessionManager sessionManager;
    @Mock private TriggerCollectionService triggerCollectionService;
    @Mock private BattlefieldEntryService battlefieldEntryService;
    @Mock private ExileService exileService;
    @InjectMocks
    private ExileSupport exileSupport;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private KnowledgePoolExileAndCastEffectHandler knowledgePoolExileAndCastHandler;

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
        InteractionHandlerRegistry registry = new InteractionHandlerRegistry();
        registry.register(new KnowledgePoolCastChoiceInteractionHandler(sessionManager, cardViewFactory, exileSupport));
        knowledgePoolExileAndCastHandler = new KnowledgePoolExileAndCastEffectHandler(gameQueryService, gameBroadcastService, registry, exileService);

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

    @Test
            @DisplayName("Fizzles when Knowledge Pool is no longer on battlefield")
            void fizzlesWhenKPGone() {
                UUID kpId = UUID.randomUUID();
                Card sourceCard = createCard("Knowledge Pool");

                KnowledgePoolExileAndCastEffect effect =
                        new KnowledgePoolExileAndCastEffect(UUID.randomUUID(), kpId, player1Id);
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, sourceCard, player1Id, sourceCard.getName(),
                        List.of(effect)
                );

                when(gameQueryService.findPermanentById(gd, kpId)).thenReturn(null);

                knowledgePoolExileAndCastHandler.resolve(gd, entry, effect);

                verify(gameBroadcastService, never()).logAndBroadcast(any(), anyString());
            }

            @Test
            @DisplayName("Logs and returns when original spell is no longer on the stack")
            void fizzlesWhenOriginalSpellGone() {
                Card kpCard = createCard("Knowledge Pool");
                Permanent kp = new Permanent(kpCard);

                KnowledgePoolExileAndCastEffect effect =
                        new KnowledgePoolExileAndCastEffect(UUID.randomUUID(), kp.getId(), player1Id);
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, kpCard, player1Id, kpCard.getName(),
                        List.of(effect)
                );

                when(gameQueryService.findPermanentById(gd, kp.getId())).thenReturn(kp);

                knowledgePoolExileAndCastHandler.resolve(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd),
                        eq("Knowledge Pool's ability — original spell is no longer on the stack."));
            }

            @Test
            @DisplayName("Exiles original spell and logs no-choice when pool has no eligible cards")
            void noEligibleCardsInPool() {
                Card kpCard = createCard("Knowledge Pool");
                Permanent kp = new Permanent(kpCard);

                Card originalCard = createSorceryCard("Lightning Bolt");
                StackEntry originalSpell = new StackEntry(
                        StackEntryType.SORCERY_SPELL, originalCard, player1Id, originalCard.getName(),
                        List.of()
                );
                gd.stack.add(originalSpell);

                KnowledgePoolExileAndCastEffect effect =
                        new KnowledgePoolExileAndCastEffect(originalCard.getId(), kp.getId(), player1Id);
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, kpCard, player1Id, kpCard.getName(),
                        List.of(effect)
                );

                when(gameQueryService.findPermanentById(gd, kp.getId())).thenReturn(kp);
                stubExileCardTrackedWithSource();

                knowledgePoolExileAndCastHandler.resolve(gd, entry, effect);

                // Original spell removed from stack
                assertThat(gd.stack).doesNotContain(originalSpell);
                // Original card added to pool and exile
                assertThat(gd.getCardsExiledByPermanent(kp.getId())).contains(originalCard);
                verify(exileService).exileCard(gd, player1Id, originalCard, kp.getId());
                // No eligible cards â†’ log message
                verify(gameBroadcastService).logAndBroadcast(eq(gd),
                        eq("Knowledge Pool — no other nonland cards exiled. Player1 cannot cast a spell."));
            }

            @Test
            @DisplayName("Presents choice when pool has eligible nonland cards")
            void presentsChoiceWhenEligibleCardsExist() {
                Card kpCard = createCard("Knowledge Pool");
                Permanent kp = new Permanent(kpCard);

                // Pre-seed pool with an eligible card
                Card poolCard = createSorceryCard("Doom Blade");
                gd.addToExile(player1Id, poolCard, kp.getId());

                Card originalCard = createSorceryCard("Lightning Bolt");
                StackEntry originalSpell = new StackEntry(
                        StackEntryType.SORCERY_SPELL, originalCard, player1Id, originalCard.getName(),
                        List.of()
                );
                gd.stack.add(originalSpell);

                KnowledgePoolExileAndCastEffect effect =
                        new KnowledgePoolExileAndCastEffect(originalCard.getId(), kp.getId(), player1Id);
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, kpCard, player1Id, kpCard.getName(),
                        List.of(effect)
                );

                CardView mockCardView = mock(CardView.class);
                when(gameQueryService.findPermanentById(gd, kp.getId())).thenReturn(kp);
                when(cardViewFactory.create(poolCard)).thenReturn(mockCardView);

                knowledgePoolExileAndCastHandler.resolve(gd, entry, effect);

                assertThat(gd.peekPendingInteraction(PendingKnowledgePoolCast.class).sourcePermanentId()).isEqualTo(kp.getId());
                assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.KnowledgePoolCastChoice.class);
                assertThat(gd.interaction.activeInteraction(PendingInteraction.KnowledgePoolCastChoice.class)
                        .validCardIds()).containsExactly(poolCard.getId());
                verify(sessionManager).sendToPlayer(eq(player1Id), any());
            }


    @Test
            @DisplayName("Player declines — logs and broadcasts")
            void playerDeclines() {
                Player player = new Player(player1Id, "Player1");
                UUID kpId = UUID.randomUUID();
                gd.queueInteraction(new PendingKnowledgePoolCast(kpId));
                gd.interaction.beginInteraction(new PendingInteraction.KnowledgePoolCastChoice(player1Id, List.of(), 1));

                exileSupport.handleKnowledgePoolCastChoice(gd, player, List.of());

                verify(gameBroadcastService).logAndBroadcast(eq(gd),
                        eq("Player1 declines to cast a spell from Knowledge Pool."));
                verify(gameBroadcastService).broadcastGameState(gd);
                assertThat(gd.peekPendingInteraction(PendingKnowledgePoolCast.class)).isNull();
            }

            @Test
            @DisplayName("Non-targeted spell is put directly on the stack")
            void nonTargetedSpellOnStack() {
                Player player = new Player(player1Id, "Player1");
                Card chosenCard = createSorceryCard("Wrath of God");
                UUID kpId = UUID.randomUUID();

                gd.queueInteraction(new PendingKnowledgePoolCast(kpId));
                gd.addToExile(player1Id, chosenCard, kpId);
                gd.interaction.beginInteraction(new PendingInteraction.KnowledgePoolCastChoice(player1Id, List.of(chosenCard.getId()), 1));

                exileSupport.handleKnowledgePoolCastChoice(gd, player, List.of(chosenCard.getId()));

                // Card removed from pool and exile
                assertThat(gd.getCardsExiledByPermanent(kpId)).doesNotContain(chosenCard);
                assertThat(gd.getPlayerExiledCards(player1Id)).doesNotContain(chosenCard);
                // Spell on stack
                assertThat(gd.stack).anyMatch(se -> se.getCard() == chosenCard);
                assertThat(gd.getSpellsCastThisTurnCount(player1Id)).isEqualTo(1);
                verify(triggerCollectionService).checkSpellCastTriggers(gd, chosenCard, player1Id, false);
                verify(gameBroadcastService).broadcastGameState(gd);
            }

            @Test
            @DisplayName("Player-only targeting spell only offers players as targets, not creatures")
            void playerOnlySpellDoesNotOfferCreatureTargets() {
                Player player = new Player(player1Id, "Player1");
                Card chosenCard = createSorceryCard("Traumatize");
                chosenCard.addEffect(EffectSlot.SPELL, new MillHalfLibraryEffect(false));
                UUID kpId = UUID.randomUUID();

                gd.queueInteraction(new PendingKnowledgePoolCast(kpId));
                gd.addToExile(player1Id, chosenCard, kpId);
                gd.interaction.beginInteraction(new PendingInteraction.KnowledgePoolCastChoice(player1Id, List.of(chosenCard.getId()), 1));

                // Put a creature on the battlefield — it must NOT appear as a valid target
                Card creatureCard = createCreatureCard("Baneslayer Angel");
                addPermanent(player2Id, creatureCard);

                exileSupport.handleKnowledgePoolCastChoice(gd, player, List.of(chosenCard.getId()));

                // Should begin permanent choice with only the two player IDs
                @SuppressWarnings("unchecked")
                org.mockito.ArgumentCaptor<List<UUID>> targetsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
                verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id), targetsCaptor.capture(), anyString());
                List<UUID> validTargets = targetsCaptor.getValue();
                assertThat(validTargets).containsExactlyInAnyOrder(player1Id, player2Id);
            }

            @Test
            @DisplayName("Broadcasts and returns when pool is null")
            void broadcastsWhenPoolNull() {
                Player player = new Player(player1Id, "Player1");
                UUID kpId = UUID.randomUUID();
                gd.queueInteraction(new PendingKnowledgePoolCast(kpId));
                gd.interaction.beginInteraction(new PendingInteraction.KnowledgePoolCastChoice(player1Id, List.of(), 1));

                exileSupport.handleKnowledgePoolCastChoice(gd, player, List.of(UUID.randomUUID()));

                verify(gameBroadcastService).broadcastGameState(gd);
            }
}
