package com.github.laxika.magicalvibes.service.library;

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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryRevealResolutionServiceTest {

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

    @InjectMocks
    private LibraryRevealResolutionService service;

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
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.activePlayerId = player1Id;
    }

    // =========================================================================
    // Helpers
    // =========================================================================

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

    @Nested
    @DisplayName("resolveRevealTopCardOfLibrary")
    class ResolveRevealTopCardOfLibrary {

        @Test
        @DisplayName("Reveals top card name in log")
        void revealsTopCard() {
            Card topCard = createCard("Grizzly Bears");
            gd.playerDecks.get(player2Id).add(topCard);

            RevealTopCardOfLibraryEffect effect = new RevealTopCardOfLibraryEffect();
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Aven Windreader"),
                    player1Id, "Aven Windreader", List.of(effect), 0,
                    player2Id, null);

            service.resolveRevealTopCardOfLibrary(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("reveals") && msg.contains("Grizzly Bears")));
        }

        @Test
        @DisplayName("Empty library logs appropriately")
        void emptyLibraryLogged() {
            RevealTopCardOfLibraryEffect effect = new RevealTopCardOfLibraryEffect();
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Aven Windreader"),
                    player1Id, "Aven Windreader", List.of(effect), 0,
                    player2Id, null);

            service.resolveRevealTopCardOfLibrary(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("library is empty")));
        }
    }

    // =========================================================================
    // resolveLookAtTopCardMayRevealTypeTransform
    // =========================================================================

    @Nested
    @DisplayName("resolveLookAtTopCardMayRevealTypeTransform")
    class ResolveLookAtTopCardMayRevealTypeTransform {

        @Test
        @DisplayName("Empty library logs and returns")
        void emptyLibraryLogs() {
            LookAtTopCardMayRevealTypeTransformEffect effect =
                    new LookAtTopCardMayRevealTypeTransformEffect(Set.of(CardType.INSTANT, CardType.SORCERY));
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Delver of Secrets"),
                    player1Id, "Delver of Secrets", List.of(effect));

            service.resolveLookAtTopCardMayRevealTypeTransform(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("library is empty")));
            assertThat(gd.pendingMayAbilities).isEmpty();
        }

        @Test
        @DisplayName("Queues may ability to reveal top card")
        void queuesMayAbility() {
            gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt", CardType.INSTANT));

            LookAtTopCardMayRevealTypeTransformEffect effect =
                    new LookAtTopCardMayRevealTypeTransformEffect(Set.of(CardType.INSTANT, CardType.SORCERY));
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Delver of Secrets"),
                    player1Id, "Delver of Secrets", List.of(effect));

            service.resolveLookAtTopCardMayRevealTypeTransform(gd, entry, effect);

            assertThat(gd.pendingMayAbilities).hasSize(1);
            assertThat(gd.pendingMayAbilities.getFirst().description()).contains("Reveal").contains("Lightning Bolt");
        }
    }

    // =========================================================================
    // resolveCastTopOfLibraryWithoutPaying
    // =========================================================================

    @Nested
    @DisplayName("resolveCastTopOfLibraryWithoutPaying")
    class ResolveCastTopOfLibraryWithoutPaying {

        @Test
        @DisplayName("Empty library logs and returns")
        void emptyLibraryLogs() {
            CastTopOfLibraryWithoutPayingManaCostEffect effect =
                    new CastTopOfLibraryWithoutPayingManaCostEffect(Set.of(CardType.INSTANT, CardType.SORCERY));
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Galvanoth"),
                    player1Id, "Galvanoth", List.of(effect));

            service.resolveCastTopOfLibraryWithoutPaying(gd, entry, effect);

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

            service.resolveCastTopOfLibraryWithoutPaying(gd, entry, effect);

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

            service.resolveCastTopOfLibraryWithoutPaying(gd, entry, effect);

            assertThat(gd.pendingMayAbilities).hasSize(1);
            assertThat(gd.pendingMayAbilities.getFirst().description()).contains("Cast").contains("Lightning Bolt");
        }
    }

    // =========================================================================
    // resolveRevealTopCardMayPlayFreeOrExile
    // =========================================================================

    @Nested
    @DisplayName("resolveRevealTopCardMayPlayFreeOrExile")
    class ResolveRevealTopCardMayPlayFreeOrExile {

        @Test
        @DisplayName("Empty library logs and returns")
        void emptyLibraryLogs() {
            RevealTopCardMayPlayFreeOrExileEffect effect = new RevealTopCardMayPlayFreeOrExileEffect();
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Djinn of Wishes"),
                    player1Id, "Djinn of Wishes", List.of(effect));

            service.resolveRevealTopCardMayPlayFreeOrExile(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("library is empty")));
            assertThat(gd.pendingMayAbilities).isEmpty();
        }

        @Test
        @DisplayName("Land card exiled when not controller's turn")
        void landExiledNotControllersTurn() {
            Card land = createCard("Forest", CardType.LAND);
            gd.playerDecks.get(player1Id).add(land);
            gd.activePlayerId = player2Id; // not controller's turn

            RevealTopCardMayPlayFreeOrExileEffect effect = new RevealTopCardMayPlayFreeOrExileEffect();
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Djinn of Wishes"),
                    player1Id, "Djinn of Wishes", List.of(effect));

            service.resolveRevealTopCardMayPlayFreeOrExile(gd, entry, effect);

            verify(exileService).exileCard(gd, player1Id, land);
            assertThat(gd.playerDecks.get(player1Id)).isEmpty();
            assertThat(gd.pendingMayAbilities).isEmpty();
        }

        @Test
        @DisplayName("Land card exiled when land already played this turn")
        void landExiledAlreadyPlayed() {
            Card land = createCard("Forest", CardType.LAND);
            gd.playerDecks.get(player1Id).add(land);
            gd.activePlayerId = player1Id;
            gd.landsPlayedThisTurn.put(player1Id, 1);

            RevealTopCardMayPlayFreeOrExileEffect effect = new RevealTopCardMayPlayFreeOrExileEffect();
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Djinn of Wishes"),
                    player1Id, "Djinn of Wishes", List.of(effect));

            service.resolveRevealTopCardMayPlayFreeOrExile(gd, entry, effect);

            verify(exileService).exileCard(gd, player1Id, land);
            assertThat(gd.pendingMayAbilities).isEmpty();
        }

        @Test
        @DisplayName("Playable card queues may ability")
        void playableCardQueuesMayAbility() {
            gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt", CardType.INSTANT));

            RevealTopCardMayPlayFreeOrExileEffect effect = new RevealTopCardMayPlayFreeOrExileEffect();
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Djinn of Wishes"),
                    player1Id, "Djinn of Wishes", List.of(effect));

            service.resolveRevealTopCardMayPlayFreeOrExile(gd, entry, effect);

            assertThat(gd.pendingMayAbilities).hasSize(1);
            assertThat(gd.pendingMayAbilities.getFirst().description()).contains("Play").contains("Lightning Bolt");
        }
    }

    // =========================================================================
    // resolveReorderTopCardsOfLibrary
    // =========================================================================

    @Nested
    @DisplayName("resolveReorderTopCardsOfLibrary")
    class ResolveReorderTopCardsOfLibrary {

        @Test
        @DisplayName("Empty library skips reorder")
        void emptyLibrarySkipsReorder() {
            ReorderTopCardsOfLibraryEffect effect = new ReorderTopCardsOfLibraryEffect(2);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Sage Owl"),
                    player1Id, "Sage Owl", List.of(effect));

            service.resolveReorderTopCardsOfLibrary(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("library is empty")));
            assertThat(gd.interaction.awaitingInputType()).isNull();
        }

        @Test
        @DisplayName("Single card skips reorder prompt")
        void singleCardSkipsReorder() {
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));

            ReorderTopCardsOfLibraryEffect effect = new ReorderTopCardsOfLibraryEffect(2);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Sage Owl"),
                    player1Id, "Sage Owl", List.of(effect));

            service.resolveReorderTopCardsOfLibrary(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("looks at the top card")));
            assertThat(gd.interaction.awaitingInputType()).isNull();
        }

        @Test
        @DisplayName("Multiple cards enters LIBRARY_REORDER state")
        void multipleCardsEntersReorderState() {
            stubCardViewFactory();
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));
            gd.playerDecks.get(player1Id).add(createCard("Llanowar Elves"));

            ReorderTopCardsOfLibraryEffect effect = new ReorderTopCardsOfLibraryEffect(2);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Sage Owl"),
                    player1Id, "Sage Owl", List.of(effect));

            service.resolveReorderTopCardsOfLibrary(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
            assertThat(gd.interaction.libraryView().reorderPlayerId()).isEqualTo(player1Id);
            verify(sessionManager).sendToPlayer(eq(player1Id), any());
        }
    }

    // =========================================================================
    // resolveScry
    // =========================================================================

    @Nested
    @DisplayName("resolveScry")
    class ResolveScry {

        @Test
        @DisplayName("Scry 0 does nothing")
        void scryZeroDoesNothing() {
            ScryEffect effect = new ScryEffect(0);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Some Card"),
                    player1Id, "Some Card", List.of(effect));

            service.resolveScry(gd, entry, effect);

            verify(gameBroadcastService, never()).logAndBroadcast(any(), any());
        }

        @Test
        @DisplayName("Empty library logs scry with empty library")
        void emptyLibraryLogs() {
            ScryEffect effect = new ScryEffect(2);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Some Card"),
                    player1Id, "Some Card", List.of(effect));

            service.resolveScry(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("scries") && msg.contains("library is empty")));
        }

        @Test
        @DisplayName("Scry 1 enters SCRY state")
        void scryOneEntersScryState() {
            stubCardViewFactory();
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));

            ScryEffect effect = new ScryEffect(1);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Some Card"),
                    player1Id, "Some Card", List.of(effect));

            service.resolveScry(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.SCRY);
            verify(sessionManager).sendToPlayer(eq(player1Id), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("scries 1")));
        }

        @Test
        @DisplayName("Scry N enters SCRY state with multiple cards")
        void scryMultipleEntersScryState() {
            stubCardViewFactory();
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));
            gd.playerDecks.get(player1Id).add(createCard("Llanowar Elves"));
            gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt"));

            ScryEffect effect = new ScryEffect(3);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Some Card"),
                    player1Id, "Some Card", List.of(effect));

            service.resolveScry(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.SCRY);
            verify(sessionManager).sendToPlayer(eq(player1Id), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("scries 3")));
        }
    }

    // =========================================================================
    // resolveAjaniUltimate
    // =========================================================================

    @Nested
    @DisplayName("resolveAjaniUltimate")
    class ResolveAjaniUltimate {

        @Test
        @DisplayName("Empty library shuffles and logs")
        void emptyLibraryShufflesAndLogs() {
            AjaniUltimateEffect effect = new AjaniUltimateEffect();
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Ajani Goldmane"),
                    player1Id, "Ajani Goldmane", List.of(effect));

            service.resolveAjaniUltimate(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("library is empty") || msg.contains("no cards")));
        }

        @Test
        @DisplayName("No eligible cards puts all back and shuffles")
        void noEligibleCardsPutsAllBack() {
            // Add only instants (not nonland permanents) — not eligible
            gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt", CardType.INSTANT, "{R}"));

            AjaniUltimateEffect effect = new AjaniUltimateEffect();
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Ajani Goldmane"),
                    player1Id, "Ajani Goldmane", List.of(effect));

            service.resolveAjaniUltimate(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no eligible cards")));
            // Card should be put back into deck
            assertThat(gd.playerDecks.get(player1Id)).hasSize(1);
        }

        @Test
        @DisplayName("Eligible cards enter LIBRARY_REVEAL_CHOICE state")
        void eligibleCardsEnterChoice() {
            stubCardViewFactory();
            // Creature with MV <= 3 is eligible
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears", CardType.CREATURE, "{1}{G}"));

            AjaniUltimateEffect effect = new AjaniUltimateEffect();
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Ajani Goldmane"),
                    player1Id, "Ajani Goldmane", List.of(effect));

            service.resolveAjaniUltimate(gd, entry);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);
            verify(sessionManager).sendToPlayer(eq(player1Id), any());
        }
    }

    // =========================================================================
    // resolveLookAtTopCardsHandTopBottom
    // =========================================================================

    @Nested
    @DisplayName("resolveLookAtTopCardsHandTopBottom")
    class ResolveLookAtTopCardsHandTopBottom {

        @Test
        @DisplayName("Empty library does nothing")
        void emptyLibraryDoesNothing() {
            LookAtTopCardsHandTopBottomEffect effect = new LookAtTopCardsHandTopBottomEffect(3);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, createCard("Telling Time"),
                    player1Id, "Telling Time", List.of(effect));

            service.resolveLookAtTopCardsHandTopBottom(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isNull();
            assertThat(gd.playerHands.get(player1Id)).isEmpty();
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("library is empty")));
        }

        @Test
        @DisplayName("Single card automatically goes to hand")
        void singleCardAutoToHand() {
            Card singleCard = createCard("Grizzly Bears");
            gd.playerDecks.get(player1Id).add(singleCard);

            LookAtTopCardsHandTopBottomEffect effect = new LookAtTopCardsHandTopBottomEffect(3);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, createCard("Telling Time"),
                    player1Id, "Telling Time", List.of(effect));

            service.resolveLookAtTopCardsHandTopBottom(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isNull();
            assertThat(gd.playerHands.get(player1Id)).contains(singleCard);
            assertThat(gd.playerDecks.get(player1Id)).isEmpty();
        }

        @Test
        @DisplayName("Multiple cards enters HAND_TOP_BOTTOM_CHOICE state")
        void multipleCardsEntersChoice() {
            stubCardViewFactory();
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));
            gd.playerDecks.get(player1Id).add(createCard("Llanowar Elves"));
            gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt"));

            LookAtTopCardsHandTopBottomEffect effect = new LookAtTopCardsHandTopBottomEffect(3);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, createCard("Telling Time"),
                    player1Id, "Telling Time", List.of(effect));

            service.resolveLookAtTopCardsHandTopBottom(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.HAND_TOP_BOTTOM_CHOICE);
            assertThat(gd.interaction.libraryView().handTopBottomPlayerId()).isEqualTo(player1Id);
            assertThat(gd.interaction.libraryView().handTopBottomCards()).hasSize(3);
            verify(sessionManager).sendToPlayer(eq(player1Id), any());
        }
    }

    // =========================================================================
    // resolveLookAtTopCardsChooseNToHandRestToGraveyard
    // =========================================================================

    @Nested
    @DisplayName("resolveLookAtTopCardsChooseNToHandRestToGraveyard")
    class ResolveLookAtTopCardsChooseOneToHandRestToGraveyard {

        @Test
        @DisplayName("Empty library logs")
        void emptyLibraryLogs() {
            LookAtTopCardsChooseNToHandRestToGraveyardEffect effect =
                    new LookAtTopCardsChooseNToHandRestToGraveyardEffect(4, 1);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, createCard("Forbidden Alchemy"),
                    player1Id, "Forbidden Alchemy", List.of(effect));

            service.resolveLookAtTopCardsChooseNToHandRestToGraveyard(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("library is empty")));
        }

        @Test
        @DisplayName("Single card goes directly to hand")
        void singleCardGoesToHand() {
            Card singleCard = createCard("Grizzly Bears");
            gd.playerDecks.get(player1Id).add(singleCard);

            LookAtTopCardsChooseNToHandRestToGraveyardEffect effect =
                    new LookAtTopCardsChooseNToHandRestToGraveyardEffect(4, 1);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, createCard("Forbidden Alchemy"),
                    player1Id, "Forbidden Alchemy", List.of(effect));

            service.resolveLookAtTopCardsChooseNToHandRestToGraveyard(gd, entry, effect);

            assertThat(gd.playerHands.get(player1Id)).contains(singleCard);
            assertThat(gd.playerDecks.get(player1Id)).isEmpty();
        }

        @Test
        @DisplayName("Multiple cards enters LIBRARY_REVEAL_CHOICE state")
        void multipleCardsEntersChoice() {
            stubCardViewFactory();
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));
            gd.playerDecks.get(player1Id).add(createCard("Llanowar Elves"));
            gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt"));
            gd.playerDecks.get(player1Id).add(createCard("Giant Growth"));

            LookAtTopCardsChooseNToHandRestToGraveyardEffect effect =
                    new LookAtTopCardsChooseNToHandRestToGraveyardEffect(4, 1);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, createCard("Forbidden Alchemy"),
                    player1Id, "Forbidden Alchemy", List.of(effect));

            service.resolveLookAtTopCardsChooseNToHandRestToGraveyard(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);
            verify(sessionManager).sendToPlayer(eq(player1Id), any());
        }
    }

    // =========================================================================
    // resolveLookAtTopCardsPerChargeCounter
    // =========================================================================

    @Nested
    @DisplayName("resolveLookAtTopCardsPerChargeCounter")
    class ResolveLookAtTopCardsPerChargeCounter {

        @Test
        @DisplayName("No charge counters logs and returns")
        void noChargeCountersLogs() {
            LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect effect =
                    new LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect();
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Shrine of Piercing Vision"),
                    player1Id, "Shrine of Piercing Vision", List.of(effect), 0);

            service.resolveLookAtTopCardsPerChargeCounter(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no charge counters")));
        }

        @Test
        @DisplayName("Empty library with counters logs")
        void emptyLibraryLogs() {
            LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect effect =
                    new LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect();
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Shrine of Piercing Vision"),
                    player1Id, "Shrine of Piercing Vision", List.of(effect), 3);

            service.resolveLookAtTopCardsPerChargeCounter(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("library is empty")));
        }

        @Test
        @DisplayName("Single card goes directly to hand")
        void singleCardGoesToHand() {
            Card singleCard = createCard("Grizzly Bears");
            gd.playerDecks.get(player1Id).add(singleCard);

            LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect effect =
                    new LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect();
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Shrine of Piercing Vision"),
                    player1Id, "Shrine of Piercing Vision", List.of(effect), 3);

            service.resolveLookAtTopCardsPerChargeCounter(gd, entry);

            assertThat(gd.playerHands.get(player1Id)).contains(singleCard);
        }

        @Test
        @DisplayName("Multiple cards enters HAND_TOP_BOTTOM_CHOICE state")
        void multipleCardsEntersChoice() {
            stubCardViewFactory();
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));
            gd.playerDecks.get(player1Id).add(createCard("Llanowar Elves"));

            LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect effect =
                    new LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect();
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Shrine of Piercing Vision"),
                    player1Id, "Shrine of Piercing Vision", List.of(effect), 3);

            service.resolveLookAtTopCardsPerChargeCounter(gd, entry);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.HAND_TOP_BOTTOM_CHOICE);
            verify(sessionManager).sendToPlayer(eq(player1Id), any());
        }
    }

    // =========================================================================
    // resolveLookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottom
    // =========================================================================

    @Nested
    @DisplayName("resolveLookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottom")
    class ResolveLookAtTopCardsMayRevealByPredicate {

        @Test
        @DisplayName("Empty library logs")
        void emptyLibraryLogs() {
            LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect effect =
                    new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(3, new CardTypePredicate(CardType.CREATURE));
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Mul Daya Channelers"),
                    player1Id, "Mul Daya Channelers", List.of(effect));

            service.resolveLookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottom(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("library is empty")));
        }

        @Test
        @DisplayName("No matching creatures reorders remaining to bottom")
        void noCreaturesReordersToBottom() {
            stubCardViewFactory();
            gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt", CardType.INSTANT));
            gd.playerDecks.get(player1Id).add(createCard("Giant Growth", CardType.INSTANT));

            LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect effect =
                    new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(3, new CardTypePredicate(CardType.CREATURE));
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Mul Daya Channelers"),
                    player1Id, "Mul Daya Channelers", List.of(effect));

            service.resolveLookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottom(gd, entry, effect);

            // With 2 cards and no creatures, should begin library reorder (to bottom)
            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        }

        @Test
        @DisplayName("Single non-creature card put back on bottom without reorder prompt")
        void singleNonCreaturePutOnBottom() {
            gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt", CardType.INSTANT));

            LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect effect =
                    new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(3, new CardTypePredicate(CardType.CREATURE));
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Mul Daya Channelers"),
                    player1Id, "Mul Daya Channelers", List.of(effect));

            service.resolveLookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottom(gd, entry, effect);

            // Single card goes back to deck without reorder prompt
            assertThat(gd.playerDecks.get(player1Id)).hasSize(1);
            assertThat(gd.interaction.awaitingInputType()).isNull();
        }

        @Test
        @DisplayName("Has matching creature enters LIBRARY_SEARCH state")
        void hasCreatureEntersSearchState() {
            stubCardViewFactory();
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears", CardType.CREATURE));
            gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt", CardType.INSTANT));
            gd.playerDecks.get(player1Id).add(createCard("Giant Growth", CardType.INSTANT));

            when(gameQueryService.matchesCardPredicate(any(), any(), any())).thenCallRealMethod();
            when(gameQueryService.matchesCardPredicate(any(), any(), any(), any(), any())).thenCallRealMethod();

            LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect effect =
                    new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(3, new CardTypePredicate(CardType.CREATURE));
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Mul Daya Channelers"),
                    player1Id, "Mul Daya Channelers", List.of(effect));

            service.resolveLookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottom(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
            verify(sessionManager).sendToPlayer(eq(player1Id), any());
        }

        @Test
        @DisplayName("anyNumber=true enters LIBRARY_REVEAL_CHOICE state")
        void anyNumberEntersRevealChoice() {
            stubCardViewFactory();
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears", CardType.CREATURE));
            gd.playerDecks.get(player1Id).add(createCard("Llanowar Elves", CardType.CREATURE));
            gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt", CardType.INSTANT));

            when(gameQueryService.matchesCardPredicate(any(), any(), any())).thenCallRealMethod();
            when(gameQueryService.matchesCardPredicate(any(), any(), any(), any(), any())).thenCallRealMethod();

            LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect effect =
                    new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(3, new CardTypePredicate(CardType.CREATURE), true);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Lead the Stampede"),
                    player1Id, "Lead the Stampede", List.of(effect));

            service.resolveLookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottom(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);
            verify(sessionManager).sendToPlayer(eq(player1Id), any());
        }
    }

    // =========================================================================
    // resolveLookAtTopCardsPutMatchingPermanentNameOnBattlefield
    // =========================================================================

    @Nested
    @DisplayName("resolveLookAtTopCardsPutMatchingPermanentNameOnBattlefield")
    class ResolveLookAtTopCardsPutMatchingPermanentName {

        @Test
        @DisplayName("Empty library logs")
        void emptyLibraryLogs() {
            LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect effect =
                    new LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect(4);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Lurking Predators"),
                    player1Id, "Lurking Predators", List.of(effect));

            service.resolveLookAtTopCardsPutMatchingPermanentNameOnBattlefield(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("library is empty")));
        }

        @Test
        @DisplayName("No matching names reorders to bottom")
        void noMatchingNamesReordersToBottom() {
            stubCardViewFactory();
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));
            gd.playerDecks.get(player1Id).add(createCard("Llanowar Elves"));
            // No permanents on battlefield matching those names

            LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect effect =
                    new LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect(4);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Some Card"),
                    player1Id, "Some Card", List.of(effect));

            service.resolveLookAtTopCardsPutMatchingPermanentNameOnBattlefield(gd, entry, effect);

            // No matching permanent names → reorder remaining to bottom
            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        }

        @Test
        @DisplayName("Matching permanent name enters LIBRARY_SEARCH state")
        void matchingNameEntersSearchState() {
            stubCardViewFactory();
            // Put a permanent on battlefield named "Grizzly Bears"
            Card bfCard = createCard("Grizzly Bears");
            Permanent perm = new Permanent(bfCard);
            gd.playerBattlefields.get(player1Id).add(perm);

            // Top of library has a card with the same name
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));
            gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt"));

            LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect effect =
                    new LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect(4);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Some Card"),
                    player1Id, "Some Card", List.of(effect));

            service.resolveLookAtTopCardsPutMatchingPermanentNameOnBattlefield(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
            verify(sessionManager).sendToPlayer(eq(player1Id), any());
        }
    }

    // =========================================================================
    // resolveImprintFromTopCards
    // =========================================================================

    @Nested
    @DisplayName("resolveImprintFromTopCards")
    class ResolveImprintFromTopCards {

        @Test
        @DisplayName("Empty library logs and does nothing")
        void emptyLibraryLogs() {
            ImprintFromTopCardsEffect effect = new ImprintFromTopCardsEffect(4);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Clone Shell"),
                    player1Id, "Clone Shell", List.of(effect));

            service.resolveImprintFromTopCards(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isNull();
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("library is empty")));
        }

        @Test
        @DisplayName("Single card is automatically exiled face down")
        void singleCardAutoExiled() {
            Card singleCard = createCard("Grizzly Bears");
            gd.playerDecks.get(player1Id).add(singleCard);

            ImprintFromTopCardsEffect effect = new ImprintFromTopCardsEffect(4);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Clone Shell"),
                    player1Id, "Clone Shell", List.of(effect));

            service.resolveImprintFromTopCards(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isNull();
            verify(exileService).exileCard(gd, player1Id, singleCard);
            assertThat(gd.playerDecks.get(player1Id)).isEmpty();
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("exiles a card face down")));
        }

        @Test
        @DisplayName("Single card sets imprint on source permanent")
        void singleCardSetsImprint() {
            Card singleCard = createCard("Grizzly Bears");
            gd.playerDecks.get(player1Id).add(singleCard);
            UUID sourcePermanentId = UUID.randomUUID();

            ImprintFromTopCardsEffect effect = new ImprintFromTopCardsEffect(4);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Clone Shell"),
                    player1Id, "Clone Shell", List.of(effect), null, sourcePermanentId);

            service.resolveImprintFromTopCards(gd, entry, effect);

            verify(gameQueryService).setImprintedCardOnPermanent(gd, sourcePermanentId, singleCard);
        }

        @Test
        @DisplayName("Multiple cards enters LIBRARY_SEARCH state for exile choice")
        void multipleCardsEntersSearchState() {
            stubCardViewFactory();
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));
            gd.playerDecks.get(player1Id).add(createCard("Llanowar Elves"));
            gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt"));
            gd.playerDecks.get(player1Id).add(createCard("Giant Growth"));

            ImprintFromTopCardsEffect effect = new ImprintFromTopCardsEffect(4);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Clone Shell"),
                    player1Id, "Clone Shell", List.of(effect));

            service.resolveImprintFromTopCards(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player1Id);
            verify(sessionManager).sendToPlayer(eq(player1Id), any());
        }
    }

    // =========================================================================
    // resolveLookAtTopCardsOfTargetLibraryMayExileOne
    // =========================================================================

    @Nested
    @DisplayName("resolveLookAtTopCardsOfTargetLibraryMayExileOne")
    class ResolveLookAtTopCardsOfTargetLibraryMayExileOne {

        @Test
        @DisplayName("Empty target library logs")
        void emptyLibraryLogs() {
            LookAtTopCardsOfTargetLibraryMayExileOneEffect effect =
                    new LookAtTopCardsOfTargetLibraryMayExileOneEffect(2);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Psychic Surgery"),
                    player1Id, "Psychic Surgery", List.of(effect), 0,
                    player2Id, null);

            service.resolveLookAtTopCardsOfTargetLibraryMayExileOne(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("library is empty")));
        }

        @Test
        @DisplayName("Has cards enters LIBRARY_SEARCH state")
        void hasCardsEntersSearchState() {
            stubCardViewFactory();
            gd.playerDecks.get(player2Id).add(createCard("Grizzly Bears"));
            gd.playerDecks.get(player2Id).add(createCard("Llanowar Elves"));

            LookAtTopCardsOfTargetLibraryMayExileOneEffect effect =
                    new LookAtTopCardsOfTargetLibraryMayExileOneEffect(2);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Psychic Surgery"),
                    player1Id, "Psychic Surgery", List.of(effect), 0,
                    player2Id, null);

            service.resolveLookAtTopCardsOfTargetLibraryMayExileOne(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
            verify(sessionManager).sendToPlayer(eq(player1Id), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("Player1") && msg.contains("Player2")));
        }
    }

    // =========================================================================
    // resolveRevealTopCardPutIntoHandAndLoseLife
    // =========================================================================

    @Nested
    @DisplayName("resolveRevealTopCardPutIntoHandAndLoseLife")
    class ResolveRevealTopCardPutIntoHandAndLoseLife {

        @Test
        @DisplayName("Empty library logs")
        void emptyLibraryLogs() {
            RevealTopCardPutIntoHandAndLoseLifeEffect effect = new RevealTopCardPutIntoHandAndLoseLifeEffect();
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Dark Tutelage"),
                    player1Id, "Dark Tutelage", List.of(effect));

            service.resolveRevealTopCardPutIntoHandAndLoseLife(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("library is empty")));
        }

        @Test
        @DisplayName("Reveals card, puts in hand, and loses life equal to mana value")
        void revealsAndLosesLife() {
            Card topCard = createCard("Grizzly Bears", CardType.CREATURE, "{1}{G}");
            gd.playerDecks.get(player1Id).add(topCard);
            when(gameQueryService.canPlayerLifeChange(gd, player1Id)).thenReturn(true);

            RevealTopCardPutIntoHandAndLoseLifeEffect effect = new RevealTopCardPutIntoHandAndLoseLifeEffect();
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Dark Tutelage"),
                    player1Id, "Dark Tutelage", List.of(effect));

            service.resolveRevealTopCardPutIntoHandAndLoseLife(gd, entry);

            assertThat(gd.playerHands.get(player1Id)).contains(topCard);
            assertThat(gd.playerDecks.get(player1Id)).isEmpty();
            // Life should decrease by 2 (MV of {1}{G})
            assertThat(gd.playerLifeTotals.get(player1Id)).isEqualTo(18);
        }

        @Test
        @DisplayName("MV 0 card does not change life total")
        void mvZeroNoLifeLoss() {
            Card topCard = createCard("Ornithopter", CardType.CREATURE, "{0}");
            gd.playerDecks.get(player1Id).add(topCard);

            RevealTopCardPutIntoHandAndLoseLifeEffect effect = new RevealTopCardPutIntoHandAndLoseLifeEffect();
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Dark Tutelage"),
                    player1Id, "Dark Tutelage", List.of(effect));

            service.resolveRevealTopCardPutIntoHandAndLoseLife(gd, entry);

            assertThat(gd.playerHands.get(player1Id)).contains(topCard);
            assertThat(gd.playerLifeTotals).doesNotContainKey(player1Id); // unchanged from default
        }

        @Test
        @DisplayName("Life can't change still puts card in hand")
        void lifeCantChangeStillDraws() {
            Card topCard = createCard("Grizzly Bears", CardType.CREATURE, "{1}{G}");
            gd.playerDecks.get(player1Id).add(topCard);
            when(gameQueryService.canPlayerLifeChange(gd, player1Id)).thenReturn(false);

            RevealTopCardPutIntoHandAndLoseLifeEffect effect = new RevealTopCardPutIntoHandAndLoseLifeEffect();
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Dark Tutelage"),
                    player1Id, "Dark Tutelage", List.of(effect));

            service.resolveRevealTopCardPutIntoHandAndLoseLife(gd, entry);

            assertThat(gd.playerHands.get(player1Id)).contains(topCard);
            assertThat(gd.playerLifeTotals).doesNotContainKey(player1Id); // life didn't change
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("life total can't change")));
        }
    }

    // =========================================================================
    // resolveEachPlayerNameCardRevealTop
    // =========================================================================

    @Nested
    @DisplayName("resolveEachPlayerNameCardRevealTop")
    class ResolveEachPlayerNameCardRevealTop {

        @Test
        @DisplayName("Begins COLOR_CHOICE state for active player in APNAP order")
        void beginsColorChoiceForActivePlayer() {
            // Put some cards in the game so card names are collected
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));

            EachPlayerNameCardRevealTopEffect effect = new EachPlayerNameCardRevealTopEffect();
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Conundrum Sphinx"),
                    player1Id, "Conundrum Sphinx", List.of(effect));

            service.resolveEachPlayerNameCardRevealTop(gd, entry);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
            verify(sessionManager).sendToPlayer(eq(player1Id), any());
        }
    }

    // =========================================================================
    // resolveRevealTopCardCreatureToBattlefieldOrMayBottom
    // =========================================================================

    @Nested
    @DisplayName("resolveRevealTopCardCreatureToBattlefieldOrMayBottom")
    class ResolveRevealTopCardCreatureToBattlefieldOrMayBottom {

        @Test
        @DisplayName("Empty library logs")
        void emptyLibraryLogs() {
            RevealTopCardCreatureToBattlefieldOrMayBottomEffect effect =
                    new RevealTopCardCreatureToBattlefieldOrMayBottomEffect();
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Lurking Predators"),
                    player1Id, "Lurking Predators", List.of(effect));

            service.resolveRevealTopCardCreatureToBattlefieldOrMayBottom(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("library is empty")));
        }

        @Test
        @DisplayName("Creature card enters battlefield")
        void creatureEntersBattlefield() {
            Card creature = createCard("Grizzly Bears", CardType.CREATURE, "{1}{G}");
            gd.playerDecks.get(player1Id).add(creature);

            RevealTopCardCreatureToBattlefieldOrMayBottomEffect effect =
                    new RevealTopCardCreatureToBattlefieldOrMayBottomEffect();
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Lurking Predators"),
                    player1Id, "Lurking Predators", List.of(effect));

            service.resolveRevealTopCardCreatureToBattlefieldOrMayBottom(gd, entry);

            assertThat(gd.playerDecks.get(player1Id)).isEmpty();
            verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(player1Id), any(Permanent.class));
            verify(battlefieldEntryService).handleCreatureEnteredBattlefield(
                    eq(gd), eq(player1Id), eq(creature), eq(null), eq(false));
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("enters the battlefield")));
        }

        @Test
        @DisplayName("Non-creature card queues may ability to put on bottom")
        void nonCreatureQueuesMayAbility() {
            Card instant = createCard("Lightning Bolt", CardType.INSTANT, "{R}");
            gd.playerDecks.get(player1Id).add(instant);

            RevealTopCardCreatureToBattlefieldOrMayBottomEffect effect =
                    new RevealTopCardCreatureToBattlefieldOrMayBottomEffect();
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Lurking Predators"),
                    player1Id, "Lurking Predators", List.of(effect));

            service.resolveRevealTopCardCreatureToBattlefieldOrMayBottom(gd, entry);

            assertThat(gd.pendingMayAbilities).hasSize(1);
            assertThat(gd.pendingMayAbilities.getFirst().description())
                    .contains("Put").contains("Lightning Bolt").contains("bottom");
            verify(battlefieldEntryService, never()).putPermanentOntoBattlefield(any(), any(), any(Permanent.class));
        }
    }

    // =========================================================================
    // resolveLookAtTopXCardsPermanentsToBattlefieldRestToGraveyard (remainingToBottomRandom)
    // =========================================================================

    @Nested
    @DisplayName("resolveLookAtTopXCards with remainingToBottomRandom")
    class ResolveLookAtTopXCardsRemainingToBottomRandom {

        private LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect createBottomRandomEffect(CardPredicate predicate) {
            return new LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect(predicate, null, true);
        }

        @Test
        @DisplayName("Empty library does nothing")
        void emptyLibraryDoesNothing() {
            var effect = createBottomRandomEffect(new CardTypePredicate(CardType.CREATURE));
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Gishath, Sun's Avatar"),
                    player1Id, "Gishath's triggered ability", List.of(effect), 7, null, null);

            service.resolveLookAtTopXCardsPermanentsToBattlefieldRestToGraveyard(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isNull();
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(s -> s.contains("library is empty")));
        }

        @Test
        @DisplayName("X=0 reveals zero cards and does nothing")
        void xZeroDoesNothing() {
            gd.playerDecks.get(player1Id).add(createCard("Some Card"));

            var effect = createBottomRandomEffect(new CardTypePredicate(CardType.CREATURE));
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Gishath, Sun's Avatar"),
                    player1Id, "Gishath's triggered ability", List.of(effect), 0, null, null);

            service.resolveLookAtTopXCardsPermanentsToBattlefieldRestToGraveyard(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isNull();
            assertThat(gd.playerDecks.get(player1Id)).hasSize(1); // untouched
        }

        @Test
        @DisplayName("No matching cards puts all on bottom immediately")
        void noMatchingCardsPutsAllOnBottom() {
            Card land = createCard("Forest", CardType.LAND);
            Card instant = createCard("Shock", CardType.INSTANT);
            gd.playerDecks.get(player1Id).add(land);
            gd.playerDecks.get(player1Id).add(instant);

            when(gameQueryService.matchesCardPredicate(any(), any(), any(), any(), any())).thenReturn(false);

            var effect = createBottomRandomEffect(new CardTypePredicate(CardType.CREATURE));
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Gishath, Sun's Avatar"),
                    player1Id, "Gishath's triggered ability", List.of(effect), 7, null, null);

            service.resolveLookAtTopXCardsPermanentsToBattlefieldRestToGraveyard(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isNull();
            // Cards should be back on the bottom of the library
            assertThat(gd.playerDecks.get(player1Id)).hasSize(2);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(s -> s.contains("no eligible cards")));
        }

        @Test
        @DisplayName("Matching cards enter LIBRARY_REVEAL_CHOICE state with randomRemainingToBottom")
        void matchingCardsEnterRevealChoiceState() {
            stubCardViewFactory();
            Card dino = createCard("Colossal Dreadmaw", CardType.CREATURE);
            Card land = createCard("Forest", CardType.LAND);
            Card instant = createCard("Shock", CardType.INSTANT);
            gd.playerDecks.get(player1Id).add(dino);
            gd.playerDecks.get(player1Id).add(land);
            gd.playerDecks.get(player1Id).add(instant);

            when(gameQueryService.matchesCardPredicate(eq(dino), any(), any(), any(), any())).thenReturn(true);
            when(gameQueryService.matchesCardPredicate(eq(land), any(), any(), any(), any())).thenReturn(false);
            when(gameQueryService.matchesCardPredicate(eq(instant), any(), any(), any(), any())).thenReturn(false);

            var effect = createBottomRandomEffect(new CardTypePredicate(CardType.CREATURE));
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Gishath, Sun's Avatar"),
                    player1Id, "Gishath's triggered ability", List.of(effect), 7, null, null);

            service.resolveLookAtTopXCardsPermanentsToBattlefieldRestToGraveyard(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);
            assertThat(gd.interaction.libraryRevealChoiceContext()).isNotNull();
            assertThat(gd.interaction.libraryRevealChoiceContext().randomRemainingToBottom()).isTrue();
            assertThat(gd.interaction.libraryRevealChoiceContext().validCardIds()).containsExactly(dino.getId());
            verify(sessionManager).sendToPlayer(eq(player1Id), any());
        }

        @Test
        @DisplayName("Reveals fewer cards when library is smaller than X")
        void fewerCardsThanX() {
            stubCardViewFactory();
            Card dino = createCard("Colossal Dreadmaw", CardType.CREATURE);
            gd.playerDecks.get(player1Id).add(dino);

            when(gameQueryService.matchesCardPredicate(eq(dino), any(), any(), any(), any())).thenReturn(true);

            var effect = createBottomRandomEffect(new CardTypePredicate(CardType.CREATURE));
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Gishath, Sun's Avatar"),
                    player1Id, "Gishath's triggered ability", List.of(effect), 7, null, null);

            service.resolveLookAtTopXCardsPermanentsToBattlefieldRestToGraveyard(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);
            // Only 1 card was in library, so only 1 revealed
            assertThat(gd.interaction.libraryRevealChoiceContext().allCards()).hasSize(1);
        }
    }
}
