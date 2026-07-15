package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExileTargetPermanentEffectHandlerTest {

    @Mock private GraveyardService graveyardService;
    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private PermanentRemovalService permanentRemovalService;
    @Mock private PlayerInputService playerInputService;
    @Mock private CardViewFactory cardViewFactory;
    @Mock private TriggerCollectionService triggerCollectionService;
    @Mock private BattlefieldEntryService battlefieldEntryService;
    @Mock private ExileService exileService;
    @Mock private DestructionSupport destructionSupport;
    @InjectMocks
    private ExileSupport exileSupport;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private ExileTargetPermanentEffectHandler exileTargetPermanentHandler;

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
        exileTargetPermanentHandler = new ExileTargetPermanentEffectHandler(gameQueryService, gameBroadcastService, permanentRemovalService, destructionSupport);

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
            @DisplayName("Exiles target permanent and logs the exile")
            void exilesTargetPermanent() {
                Card targetCard = createCard("Spellbook");
                Permanent target = new Permanent(targetCard);
                Card sourceCard = createCard("Revoke Existence");

                StackEntry entry = createSingleTargetEntry(sourceCard, player1Id, target.getId());

                when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);

                exileTargetPermanentHandler.resolve(gd, entry, entry.getEffectsToResolve().getFirst());

                verify(permanentRemovalService).removePermanentToExile(gd, target);
                verify(gameBroadcastService).logAndBroadcast(eq(gd), eq(GameLog.text("Spellbook is exiled.")));
                verify(permanentRemovalService).removeOrphanedAuras(gd);
            }

            @Test
            @DisplayName("Does nothing when target permanent is removed before resolution")
            void fizzlesWhenTargetRemoved() {
                UUID targetId = UUID.randomUUID();
                Card sourceCard = createCard("Revoke Existence");

                StackEntry entry = createSingleTargetEntry(sourceCard, player1Id, targetId);

                when(gameQueryService.findPermanentById(gd, targetId)).thenReturn(null);

                exileTargetPermanentHandler.resolve(gd, entry, entry.getEffectsToResolve().getFirst());

                verify(permanentRemovalService, never()).removePermanentToExile(any(), any());
                verify(permanentRemovalService).removeOrphanedAuras(gd);
            }


    @Test
            @DisplayName("Exiles two target artifacts")
            void exilesTwoTargetArtifacts() {
                Card targetCard1 = createCard("Spellbook");
                Permanent target1 = new Permanent(targetCard1);
                Card targetCard2 = createCard("Spellbook");
                Permanent target2 = new Permanent(targetCard2);
                Card sourceCard = createCard("Into the Core");

                StackEntry entry = createMultiTargetEntry(sourceCard, player1Id,
                        List.of(target1.getId(), target2.getId()));

                when(gameQueryService.findPermanentById(gd, target1.getId())).thenReturn(target1);
                when(gameQueryService.findPermanentById(gd, target2.getId())).thenReturn(target2);

                exileTargetPermanentHandler.resolve(gd, entry, entry.getEffectsToResolve().getFirst());

                verify(permanentRemovalService).removePermanentToExile(gd, target1);
                verify(permanentRemovalService).removePermanentToExile(gd, target2);
                verify(gameBroadcastService, times(2)).logAndBroadcast(eq(gd), eq(GameLog.text("Spellbook is exiled.")));
                verify(permanentRemovalService).removeOrphanedAuras(gd);
            }

            @Test
            @DisplayName("Skips targets that were removed before resolution and exiles the rest")
            void skipsRemovedTargets() {
                UUID removedId = UUID.randomUUID();
                Card targetCard = createCard("Spellbook");
                Permanent target2 = new Permanent(targetCard);
                Card sourceCard = createCard("Into the Core");

                StackEntry entry = createMultiTargetEntry(sourceCard, player1Id,
                        List.of(removedId, target2.getId()));

                when(gameQueryService.findPermanentById(gd, removedId)).thenReturn(null);
                when(gameQueryService.findPermanentById(gd, target2.getId())).thenReturn(target2);

                exileTargetPermanentHandler.resolve(gd, entry, entry.getEffectsToResolve().getFirst());

                verify(permanentRemovalService).removePermanentToExile(gd, target2);
                verify(permanentRemovalService).removeOrphanedAuras(gd);
            }
}
