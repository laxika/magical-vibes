package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AjaniUltimateEffect;
import com.github.laxika.magicalvibes.model.effect.CastTopOfLibraryWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerNameCardRevealTopEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintFromTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealTypeTransformEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsChooseNToHandRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsHandTopBottomEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryMayExileOneEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldOrMayBottomEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPlayFreeOrExileEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.effect.normalfx.AjaniUltimateEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.CastTopOfLibraryWithoutPayingManaCostEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachPlayerNameCardRevealTopEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ImprintFromTopCardsEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.LibraryRevealSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LookAtTopCardMayRevealTypeTransformEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.LookAtTopCardsChooseNToHandRestToGraveyardEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.LookAtTopCardsHandTopBottomEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.LookAtTopCardsOfTargetLibraryMayExileOneEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ReorderTopCardsOfLibraryEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.RevealTopCardCreatureToBattlefieldOrMayBottomEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.RevealTopCardMayPlayFreeOrExileEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.RevealTopCardOfLibraryEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.RevealTopCardPutIntoHandAndLoseLifeEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ScryEffectHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CastTopOfLibraryWithoutPayingManaCostEffectHandlerTest {

    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private GameBroadcastService gameBroadcastService;
    @Mock
    private SessionManager sessionManager;
    @Mock
    private CardViewFactory cardViewFactory;
    @Mock
    private BattlefieldEntryService battlefieldEntryService;
    @Mock
    private ExileService exileService;
    private LibraryRevealSupport libraryRevealSupport;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private CastTopOfLibraryWithoutPayingManaCostEffectHandler castTopOfLibraryWithoutPayingManaCostEffectHandler;

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
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.activePlayerId = player1Id;

        libraryRevealSupport = new LibraryRevealSupport(gameBroadcastService, sessionManager, cardViewFactory);
        castTopOfLibraryWithoutPayingManaCostEffectHandler = new CastTopOfLibraryWithoutPayingManaCostEffectHandler(gameBroadcastService);

    }

    private static Card createCard(String name) {
            Card card = new Card();
            card.setName(name);
            return card;
        }

        private static Card createCard(String name, CardType type) {
            Card card = createCard(name);
            card.setType(type);
            return card;
        }

        private static Card createCard(String name, CardType type, String manaCost) {
            Card card = createCard(name, type);
            card.setManaCost(manaCost);
            return card;
        }

        private void stubCardViewFactory() {
            lenient().when(cardViewFactory.create(any(Card.class))).thenReturn(mock(CardView.class));
        }

        // =========================================================================
        // resolveRevealTopCardOfLibrary
        // =========================================================================

    @Test
            @DisplayName("Empty library logs and returns")
            void emptyLibraryLogs() {
                CastTopOfLibraryWithoutPayingManaCostEffect effect =
                        new CastTopOfLibraryWithoutPayingManaCostEffect(Set.of(CardType.INSTANT, CardType.SORCERY));
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Galvanoth"),
                        player1Id, "Galvanoth", List.of(effect));

                castTopOfLibraryWithoutPayingManaCostEffectHandler.resolve(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("library is empty")));
                assertThat(gd.pendingMayAbilities).isEmpty();
            }

            @Test
            @DisplayName("Non-matching type logs no match")
            void nonMatchingTypeLogs() {
                gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears", CardType.CREATURE));

                CastTopOfLibraryWithoutPayingManaCostEffect effect =
                        new CastTopOfLibraryWithoutPayingManaCostEffect(Set.of(CardType.INSTANT, CardType.SORCERY));
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Galvanoth"),
                        player1Id, "Galvanoth", List.of(effect));

                castTopOfLibraryWithoutPayingManaCostEffectHandler.resolve(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("not a castable type")));
                assertThat(gd.pendingMayAbilities).isEmpty();
            }

            @Test
            @DisplayName("Matching type queues may ability to cast")
            void matchingTypeQueuesMayAbility() {
                gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt", CardType.INSTANT));

                CastTopOfLibraryWithoutPayingManaCostEffect effect =
                        new CastTopOfLibraryWithoutPayingManaCostEffect(Set.of(CardType.INSTANT, CardType.SORCERY));
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Galvanoth"),
                        player1Id, "Galvanoth", List.of(effect));

                castTopOfLibraryWithoutPayingManaCostEffectHandler.resolve(gd, entry, effect);

                assertThat(gd.pendingMayAbilities).hasSize(1);
                assertThat(gd.pendingMayAbilities.getFirst().description()).contains("Cast").contains("Lightning Bolt");
            }
}
