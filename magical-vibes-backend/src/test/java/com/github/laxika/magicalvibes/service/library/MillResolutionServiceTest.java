package com.github.laxika.magicalvibes.service.library;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMillsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsRepeatOnDuplicateEffect;
import com.github.laxika.magicalvibes.model.effect.MillBottomOfTargetLibraryConditionalTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillByHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerEffect;
import com.github.laxika.magicalvibes.model.effect.MillHalfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.PermanentControlResolutionService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MillResolutionServiceTest {

    @Mock
    private GraveyardService graveyardService;

    @Mock
    private GameBroadcastService gameBroadcastService;

    @Mock
    private PermanentControlResolutionService permanentControlResolutionService;

    @InjectMocks
    private MillResolutionService service;

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

    // =========================================================================
    // resolveMillByHandSize
    // =========================================================================

    @Nested
    @DisplayName("resolveMillByHandSize")
    class ResolveMillByHandSize {

        @Test
        @DisplayName("Mills cards equal to target player's hand size")
        void millsByHandSize() {
            gd.playerHands.get(player1Id).addAll(List.of(
                    createCard("Bear1"), createCard("Bear2"), createCard("Bear3")));

            MillByHandSizeEffect effect = new MillByHandSizeEffect();
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Dreamborn Muse"),
                    player1Id, "Dreamborn Muse", List.of(effect), 0, player1Id, null);

            service.resolveMillByHandSize(gd, entry);

            verify(graveyardService).resolveMillPlayer(gd, player1Id, 3);
        }

        @Test
        @DisplayName("Mills nothing when hand is empty")
        void millsNothingWithEmptyHand() {
            MillByHandSizeEffect effect = new MillByHandSizeEffect();
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Dreamborn Muse"),
                    player1Id, "Dreamborn Muse", List.of(effect), 0, player1Id, null);

            service.resolveMillByHandSize(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("mills nothing")));
            verifyNoInteractions(graveyardService);
        }

        @Test
        @DisplayName("Passes full hand size to graveyard service even when it exceeds library")
        void passesFullHandSize() {
            gd.playerHands.get(player1Id).addAll(List.of(
                    createCard("Bear1"), createCard("Bear2"), createCard("Bear3"),
                    createCard("Bear4"), createCard("Bear5")));
            gd.playerDecks.get(player1Id).addAll(List.of(createCard("Card1"), createCard("Card2")));

            MillByHandSizeEffect effect = new MillByHandSizeEffect();
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Dreamborn Muse"),
                    player1Id, "Dreamborn Muse", List.of(effect), 0, player1Id, null);

            service.resolveMillByHandSize(gd, entry);

            verify(graveyardService).resolveMillPlayer(gd, player1Id, 5);
        }
    }

    // =========================================================================
    // resolveMillTargetPlayerByChargeCounters
    // =========================================================================

    @Nested
    @DisplayName("resolveMillTargetPlayerByChargeCounters")
    class ResolveMillTargetPlayerByChargeCounters {

        @Test
        @DisplayName("Mills nothing with zero charge counters")
        void millsNothingWithZeroCounters() {
            MillTargetPlayerByChargeCountersEffect effect = new MillTargetPlayerByChargeCountersEffect();
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Grindclock"),
                    player1Id, "Grindclock", List.of(effect), 0, player2Id, null);

            service.resolveMillTargetPlayerByChargeCounters(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("mills 0 cards")));
            verifyNoInteractions(graveyardService);
        }

        @Test
        @DisplayName("Passes charge counter count to graveyard service")
        void millsByChargeCounters() {
            MillTargetPlayerByChargeCountersEffect effect = new MillTargetPlayerByChargeCountersEffect();
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Grindclock"),
                    player1Id, "Grindclock", List.of(effect), 10, player2Id, null);

            service.resolveMillTargetPlayerByChargeCounters(gd, entry);

            verify(graveyardService).resolveMillPlayer(gd, player2Id, 10);
        }
    }

    // =========================================================================
    // resolveMillHalfLibrary
    // =========================================================================

    @Nested
    @DisplayName("resolveMillHalfLibrary")
    class ResolveMillHalfLibrary {

        @Test
        @DisplayName("Mills half of library rounded down")
        void millsHalfRoundedDown() {
            for (int i = 0; i < 11; i++) {
                gd.playerDecks.get(player2Id).add(createCard("Card" + i));
            }

            MillHalfLibraryEffect effect = new MillHalfLibraryEffect();
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Traumatize"),
                    player1Id, "Traumatize", List.of(effect), 0, player2Id, null);

            service.resolveMillHalfLibrary(gd, entry);

            verify(graveyardService).resolveMillPlayer(gd, player2Id, 5);
        }

        @Test
        @DisplayName("Empty library does nothing")
        void emptyLibraryDoesNothing() {
            MillHalfLibraryEffect effect = new MillHalfLibraryEffect();
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Traumatize"),
                    player1Id, "Traumatize", List.of(effect), 0, player2Id, null);

            service.resolveMillHalfLibrary(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("mills nothing")));
            verifyNoInteractions(graveyardService);
        }
    }

    // =========================================================================
    // resolveMillController
    // =========================================================================

    @Nested
    @DisplayName("resolveMillController")
    class ResolveMillController {

        @Test
        @DisplayName("Mills controller for fixed count")
        void millsControllerFixedCount() {
            MillControllerEffect effect = new MillControllerEffect(4);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Armored Skaab"),
                    player1Id, "Armored Skaab", List.of(effect));

            service.resolveMillController(gd, entry, effect);

            verify(graveyardService).resolveMillPlayer(gd, player1Id, 4);
        }
    }

    // =========================================================================
    // resolveMillTargetPlayer
    // =========================================================================

    @Nested
    @DisplayName("resolveMillTargetPlayer")
    class ResolveMillTargetPlayer {

        @Test
        @DisplayName("Mills target player for fixed count")
        void millsTargetPlayerFixedCount() {
            MillTargetPlayerEffect effect = new MillTargetPlayerEffect(3);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, createCard("Thought Scour"),
                    player1Id, "Thought Scour", List.of(effect), 0, player2Id, null);

            service.resolveMillTargetPlayer(gd, entry, effect);

            verify(graveyardService).resolveMillPlayer(gd, player2Id, 3);
        }
    }

    // =========================================================================
    // resolveEachOpponentMills
    // =========================================================================

    @Nested
    @DisplayName("resolveEachOpponentMills")
    class ResolveEachOpponentMills {

        @Test
        @DisplayName("Mills each opponent but not the controller")
        void millsEachOpponentNotController() {
            EachOpponentMillsEffect effect = new EachOpponentMillsEffect(2);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Undead Alchemist"),
                    player1Id, "Undead Alchemist", List.of(effect));

            service.resolveEachOpponentMills(gd, entry, effect);

            verify(graveyardService).resolveMillPlayer(gd, player2Id, 2);
            verify(graveyardService, never()).resolveMillPlayer(eq(gd), eq(player1Id), any(int.class));
        }
    }

    // =========================================================================
    // resolveExileTopCardsRepeatOnDuplicate
    // =========================================================================

    @Nested
    @DisplayName("resolveExileTopCardsRepeatOnDuplicate")
    class ResolveExileTopCardsRepeatOnDuplicate {

        @Test
        @DisplayName("Exiles cards from top of library")
        void exilesCardsFromTop() {
            gd.playerDecks.get(player2Id).addAll(List.of(
                    createCard("Alpha"), createCard("Beta"), createCard("Gamma")));

            ExileTopCardsRepeatOnDuplicateEffect effect = new ExileTopCardsRepeatOnDuplicateEffect(2);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Muse"),
                    player1Id, "Muse", List.of(effect), 0, player2Id, null);

            service.resolveExileTopCardsRepeatOnDuplicate(gd, entry, effect);

            assertThat(gd.getPlayerExiledCards(player2Id)).hasSize(2);
            assertThat(gd.playerDecks.get(player2Id)).hasSize(1);
        }

        @Test
        @DisplayName("Repeats when exiled cards share a name")
        void repeatsOnDuplicate() {
            gd.playerDecks.get(player2Id).addAll(List.of(
                    createCard("Same"), createCard("Same"),
                    createCard("Unique1"), createCard("Unique2")));

            ExileTopCardsRepeatOnDuplicateEffect effect = new ExileTopCardsRepeatOnDuplicateEffect(2);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Muse"),
                    player1Id, "Muse", List.of(effect), 0, player2Id, null);

            service.resolveExileTopCardsRepeatOnDuplicate(gd, entry, effect);

            // First round exiles "Same", "Same" (duplicate → repeat)
            // Second round exiles "Unique1", "Unique2" (no duplicate → stop)
            assertThat(gd.getPlayerExiledCards(player2Id)).hasSize(4);
            assertThat(gd.playerDecks.get(player2Id)).isEmpty();
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("share the same name")));
        }

        @Test
        @DisplayName("Stops when library is empty")
        void stopsOnEmptyLibrary() {
            ExileTopCardsRepeatOnDuplicateEffect effect = new ExileTopCardsRepeatOnDuplicateEffect(2);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Muse"),
                    player1Id, "Muse", List.of(effect), 0, player2Id, null);

            service.resolveExileTopCardsRepeatOnDuplicate(gd, entry, effect);

            assertThat(gd.getPlayerExiledCards(player2Id)).isEmpty();
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("library is empty")));
        }
    }

    // =========================================================================
    // resolveMillBottomOfTargetLibraryConditionalToken
    // =========================================================================

    @Nested
    @DisplayName("resolveMillBottomOfTargetLibraryConditionalToken")
    class ResolveMillBottomOfTargetLibraryConditionalToken {

        private MillBottomOfTargetLibraryConditionalTokenEffect cellarDoorEffect() {
            return new MillBottomOfTargetLibraryConditionalTokenEffect(
                    CardType.CREATURE, "Zombie", 2, 2, CardColor.BLACK,
                    List.of(CardSubtype.ZOMBIE));
        }

        @Test
        @DisplayName("Empty library does nothing")
        void emptyLibraryDoesNothing() {
            MillBottomOfTargetLibraryConditionalTokenEffect effect = cellarDoorEffect();
            Card source = createCard("Cellar Door");
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, source,
                    player1Id, "Cellar Door", List.of(effect), 0, player2Id, null);

            service.resolveMillBottomOfTargetLibraryConditionalToken(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("library is empty")));
            verifyNoInteractions(graveyardService);
            verifyNoInteractions(permanentControlResolutionService);
        }

        @Test
        @DisplayName("Bottom card is creature — mills and creates token")
        void creatureCardCreatesToken() {
            Card creature = createCard("Grizzly Bears");
            creature.setType(CardType.CREATURE);
            gd.playerDecks.get(player2Id).add(creature);

            MillBottomOfTargetLibraryConditionalTokenEffect effect = cellarDoorEffect();
            Card source = createCard("Cellar Door");
            source.setSetCode("ISD");
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, source,
                    player1Id, "Cellar Door", List.of(effect), 0, player2Id, null);

            service.resolveMillBottomOfTargetLibraryConditionalToken(gd, entry, effect);

            verify(graveyardService).addCardToGraveyard(gd, player2Id, creature);
            verify(permanentControlResolutionService).applyCreateCreatureToken(
                    eq(gd), eq(player1Id), any(CreateCreatureTokenEffect.class), eq("ISD"));
            assertThat(gd.playerDecks.get(player2Id)).isEmpty();
        }

        @Test
        @DisplayName("Bottom card is non-creature — mills but no token")
        void nonCreatureCardNoToken() {
            Card land = createCard("Forest");
            land.setType(CardType.LAND);
            gd.playerDecks.get(player2Id).add(land);

            MillBottomOfTargetLibraryConditionalTokenEffect effect = cellarDoorEffect();
            Card source = createCard("Cellar Door");
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, source,
                    player1Id, "Cellar Door", List.of(effect), 0, player2Id, null);

            service.resolveMillBottomOfTargetLibraryConditionalToken(gd, entry, effect);

            verify(graveyardService).addCardToGraveyard(gd, player2Id, land);
            verifyNoInteractions(permanentControlResolutionService);
            assertThat(gd.playerDecks.get(player2Id)).isEmpty();
        }
    }
}
