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
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndImprintEffect;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExileTargetPermanentAndImprintEffectHandlerTest {

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
    private ExileTargetPermanentAndImprintEffectHandler exileTargetPermanentAndImprintHandler;

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
        exileTargetPermanentAndImprintHandler = new ExileTargetPermanentAndImprintEffectHandler(gameQueryService, gameBroadcastService, permanentRemovalService);

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
            @DisplayName("Exiles target and imprints onto source permanent")
            void exilesAndImprints() {
                Card targetCard = createCreatureCard("Grizzly Bears");
                Permanent target = new Permanent(targetCard);
                Card sourceCard = createCard("Exclusion Ritual");
                Permanent source = addPermanent(player1Id, sourceCard);

                StackEntry entry = new StackEntry(
                        StackEntryType.ENCHANTMENT_SPELL, sourceCard, player1Id, sourceCard.getName(),
                        List.of(new ExileTargetPermanentAndImprintEffect()),
                        0, target.getId(), null
                );

                when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);

                exileTargetPermanentAndImprintHandler.resolve(gd, entry, entry.getEffectsToResolve().getFirst());

                verify(permanentRemovalService).removePermanentToExile(gd, target);
                assertThat(gd.getImprintedCard(source.getCard())).isSameAs(targetCard);
                verify(gameBroadcastService).logAndBroadcast(eq(gd),
                        eq("Grizzly Bears is exiled by Exclusion Ritual."));
                verify(permanentRemovalService).removeOrphanedAuras(gd);
            }

            @Test
            @DisplayName("Does nothing when target is removed before resolution")
            void fizzlesWhenTargetRemoved() {
                UUID targetId = UUID.randomUUID();
                Card sourceCard = createCard("Exclusion Ritual");

                StackEntry entry = new StackEntry(
                        StackEntryType.ENCHANTMENT_SPELL, sourceCard, player1Id, sourceCard.getName(),
                        List.of(new ExileTargetPermanentAndImprintEffect()),
                        0, targetId, null
                );

                when(gameQueryService.findPermanentById(gd, targetId)).thenReturn(null);

                exileTargetPermanentAndImprintHandler.resolve(gd, entry, entry.getEffectsToResolve().getFirst());

                verify(permanentRemovalService, never()).removePermanentToExile(any(), any());
            }

            @Test
            @DisplayName("Still exiles when source is not on battlefield but does not imprint")
            void exilesWithoutImprintWhenSourceGone() {
                Card targetCard = createCreatureCard("Grizzly Bears");
                Permanent target = new Permanent(targetCard);
                Card sourceCard = createCard("Exclusion Ritual");
                // Source is NOT on battlefield

                StackEntry entry = new StackEntry(
                        StackEntryType.ENCHANTMENT_SPELL, sourceCard, player1Id, sourceCard.getName(),
                        List.of(new ExileTargetPermanentAndImprintEffect()),
                        0, target.getId(), null
                );

                when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);

                exileTargetPermanentAndImprintHandler.resolve(gd, entry, entry.getEffectsToResolve().getFirst());

                verify(permanentRemovalService).removePermanentToExile(gd, target);
                // No permanent to imprint onto
                verify(permanentRemovalService).removeOrphanedAuras(gd);
            }
}
