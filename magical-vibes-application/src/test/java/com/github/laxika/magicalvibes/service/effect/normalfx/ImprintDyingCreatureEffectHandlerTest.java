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
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ImprintDyingCreatureEffectHandler;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImprintDyingCreatureEffectHandlerTest {

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
    private ImprintDyingCreatureEffectHandler imprintDyingCreatureHandler;

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
        imprintDyingCreatureHandler = new ImprintDyingCreatureEffectHandler(gameQueryService, gameBroadcastService, graveyardService, exileService);

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
            @DisplayName("Imprints a dying creature onto source permanent")
            void imprintsDyingCreature() {
                Card vatCard = createCard("Mimic Vat");
                Permanent vatPerm = new Permanent(vatCard);

                Card dyingCard = createCreatureCard("Grizzly Bears");
                gd.playerGraveyards.get(player2Id).add(dyingCard);

                ImprintDyingCreatureEffect effect = new ImprintDyingCreatureEffect(dyingCard.getId());
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, vatCard, player1Id, "Mimic Vat trigger",
                        List.of(effect), vatPerm.getId(), (UUID) null
                );

                when(gameQueryService.findPermanentById(gd, vatPerm.getId())).thenReturn(vatPerm);

                imprintDyingCreatureHandler.resolve(gd, entry, effect);

                // Dying card moved from graveyard to exile
                assertThat(gd.playerGraveyards.get(player2Id))
                        .noneMatch(c -> c.getName().equals("Grizzly Bears"));
                verify(exileService).exileCard(gd, player2Id, dyingCard);
                // Card imprinted on source
                assertThat(vatPerm.getCard().getImprintedCard()).isSameAs(dyingCard);
            }

            @Test
            @DisplayName("Replaces previously imprinted card when a new creature dies")
            void replacesPreviousImprint() {
                Card vatCard = createCard("Mimic Vat");
                Permanent vatPerm = new Permanent(vatCard);

                // Set up previously imprinted card
                Card previousCard = createCreatureCard("Giant Spider");
                vatPerm.getCard().setImprintedCard(previousCard);
                gd.getPlayerExiledCards(player1Id).add(previousCard);

                // New dying creature
                Card dyingCard = createCreatureCard("Grizzly Bears");
                gd.playerGraveyards.get(player2Id).add(dyingCard);

                ImprintDyingCreatureEffect effect = new ImprintDyingCreatureEffect(dyingCard.getId());
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, vatCard, player1Id, "Mimic Vat trigger",
                        List.of(effect), vatPerm.getId(), (UUID) null
                );

                when(gameQueryService.findPermanentById(gd, vatPerm.getId())).thenReturn(vatPerm);

                imprintDyingCreatureHandler.resolve(gd, entry, effect);

                // New card should be imprinted
                assertThat(vatPerm.getCard().getImprintedCard()).isSameAs(dyingCard);
                // Previous card returned to owner's graveyard
                verify(graveyardService).addCardToGraveyard(gd, player1Id, previousCard);
                // Previous card removed from exile
                assertThat(gd.getPlayerExiledCards(player1Id))
                        .noneMatch(c -> c.getName().equals("Giant Spider"));
            }

            @Test
            @DisplayName("Does nothing when source permanent is gone")
            void fizzlesWhenSourceGone() {
                Card dyingCard = createCreatureCard("Grizzly Bears");
                gd.playerGraveyards.get(player2Id).add(dyingCard);

                UUID vatId = UUID.randomUUID();
                Card vatCard = createCard("Mimic Vat");

                ImprintDyingCreatureEffect effect = new ImprintDyingCreatureEffect(dyingCard.getId());
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, vatCard, player1Id, "Mimic Vat trigger",
                        List.of(effect), vatId, (UUID) null
                );

                when(gameQueryService.findPermanentById(gd, vatId)).thenReturn(null);

                imprintDyingCreatureHandler.resolve(gd, entry, effect);

                // Dying card should still be in graveyard
                assertThat(gd.playerGraveyards.get(player2Id))
                        .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            }

            @Test
            @DisplayName("Does nothing when dying card is no longer in graveyard")
            void fizzlesWhenDyingCardGone() {
                Card vatCard = createCard("Mimic Vat");
                Permanent vatPerm = new Permanent(vatCard);

                UUID dyingCardId = UUID.randomUUID();
                ImprintDyingCreatureEffect effect = new ImprintDyingCreatureEffect(dyingCardId);
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, vatCard, player1Id, "Mimic Vat trigger",
                        List.of(effect), vatPerm.getId(), (UUID) null
                );

                when(gameQueryService.findPermanentById(gd, vatPerm.getId())).thenReturn(vatPerm);

                imprintDyingCreatureHandler.resolve(gd, entry, effect);

                assertThat(vatPerm.getCard().getImprintedCard()).isNull();
            }

            @Test
            @DisplayName("Logs imprint message")
            void logsImprint() {
                Card vatCard = createCard("Mimic Vat");
                Permanent vatPerm = new Permanent(vatCard);

                Card dyingCard = createCreatureCard("Grizzly Bears");
                gd.playerGraveyards.get(player2Id).add(dyingCard);

                ImprintDyingCreatureEffect effect = new ImprintDyingCreatureEffect(dyingCard.getId());
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, vatCard, player1Id, "Mimic Vat trigger",
                        List.of(effect), vatPerm.getId(), (UUID) null
                );

                when(gameQueryService.findPermanentById(gd, vatPerm.getId())).thenReturn(vatPerm);

                imprintDyingCreatureHandler.resolve(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd),
                        eq("Grizzly Bears is exiled and imprinted on Mimic Vat."));
            }
}
