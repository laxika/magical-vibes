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
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileAllPermanentsEffectHandler;
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

@ExtendWith(MockitoExtension.class)
class ExileAllPermanentsEffectHandlerTest {

    @Mock private GraveyardService graveyardService;
    @Mock private GameQueryService gameQueryService;
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
    private ExileAllPermanentsEffectHandler exileAllPermanentsHandler;

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
        exileAllPermanentsHandler = new ExileAllPermanentsEffectHandler(gameQueryService, gameBroadcastService, permanentRemovalService);

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
            @DisplayName("Exiles all permanents matching the predicate across both battlefields")
            void exilesMatchingPermanents() {
                Card creature1Card = createCreatureCard("Bear");
                Permanent creature1 = addPermanent(player1Id, creature1Card);
                Card creature2Card = createCreatureCard("Elk");
                Permanent creature2 = addPermanent(player2Id, creature2Card);

                Card sourceCard = createSorceryCard("Blast");
                PermanentPredicate filter = new PermanentIsCreaturePredicate();
                ExileAllPermanentsEffect effect = new ExileAllPermanentsEffect(filter);
                StackEntry entry = new StackEntry(
                        StackEntryType.SORCERY_SPELL, sourceCard, player1Id, sourceCard.getName(),
                        List.of(effect), 0, (UUID) null, null
                );

                when(gameQueryService.matchesPermanentPredicate(any(Permanent.class), eq(filter), any()))
                        .thenReturn(true);

                exileAllPermanentsHandler.resolve(gd, entry, effect);

                verify(permanentRemovalService).removePermanentToExile(gd, creature1);
                verify(permanentRemovalService).removePermanentToExile(gd, creature2);
                verify(gameBroadcastService).logAndBroadcast(eq(gd), eq("Bear is exiled."));
                verify(gameBroadcastService).logAndBroadcast(eq(gd), eq("Elk is exiled."));
                verify(permanentRemovalService).removeOrphanedAuras(gd);
            }

            @Test
            @DisplayName("Skips permanents that do not match the predicate")
            void skipsNonMatchingPermanents() {
                Card creatureCard = createCreatureCard("Bear");
                Permanent creature = addPermanent(player1Id, creatureCard);
                Card landCard = createLandCard("Plains");
                Permanent land = addPermanent(player1Id, landCard);

                Card sourceCard = createSorceryCard("Blast");
                PermanentPredicate filter = new PermanentIsCreaturePredicate();
                ExileAllPermanentsEffect effect = new ExileAllPermanentsEffect(filter);
                StackEntry entry = new StackEntry(
                        StackEntryType.SORCERY_SPELL, sourceCard, player1Id, sourceCard.getName(),
                        List.of(effect), 0, (UUID) null, null
                );

                when(gameQueryService.matchesPermanentPredicate(eq(creature), eq(filter), any()))
                        .thenReturn(true);
                when(gameQueryService.matchesPermanentPredicate(eq(land), eq(filter), any()))
                        .thenReturn(false);

                exileAllPermanentsHandler.resolve(gd, entry, effect);

                verify(permanentRemovalService).removePermanentToExile(gd, creature);
                verify(permanentRemovalService, never()).removePermanentToExile(gd, land);
                verify(permanentRemovalService).removeOrphanedAuras(gd);
            }

            @Test
            @DisplayName("Does nothing when no permanents match")
            void nothingToExile() {
                Card sourceCard = createSorceryCard("Blast");
                PermanentPredicate filter = new PermanentIsCreaturePredicate();
                ExileAllPermanentsEffect effect = new ExileAllPermanentsEffect(filter);
                StackEntry entry = new StackEntry(
                        StackEntryType.SORCERY_SPELL, sourceCard, player1Id, sourceCard.getName(),
                        List.of(effect), 0, (UUID) null, null
                );

                exileAllPermanentsHandler.resolve(gd, entry, effect);

                verify(permanentRemovalService, never()).removePermanentToExile(any(), any());
                verify(permanentRemovalService).removeOrphanedAuras(gd);
            }
}
