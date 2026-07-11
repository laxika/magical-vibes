package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsRepeatOnDuplicateEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExileTopCardsRepeatOnDuplicateEffectHandlerTest {

    @Mock
    private GraveyardService graveyardService;
    @Mock
    private GameBroadcastService gameBroadcastService;
    @Mock
    private PermanentControlSupport permanentControlSupport;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private ExileTopCardsRepeatOnDuplicateEffectHandler exileTopCardsRepeatOnDuplicateEffectHandler;

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
        exileTopCardsRepeatOnDuplicateEffectHandler =
                new ExileTopCardsRepeatOnDuplicateEffectHandler(gameBroadcastService);

    }

    private static Card createCard(String name) {
            Card card = new Card();
            card.setName(name);
            return card;
        }

        // =========================================================================
        // resolveMillByHandSize
        // =========================================================================

    @Test
            @DisplayName("Exiles cards from top of library")
            void exilesCardsFromTop() {
                gd.playerDecks.get(player2Id).addAll(List.of(
                        createCard("Alpha"), createCard("Beta"), createCard("Gamma")));

                ExileTopCardsRepeatOnDuplicateEffect effect = new ExileTopCardsRepeatOnDuplicateEffect(2);
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Muse"),
                        player1Id, "Muse", List.of(effect), 0, player2Id, null);

                exileTopCardsRepeatOnDuplicateEffectHandler.resolve(gd, entry, effect);

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

                exileTopCardsRepeatOnDuplicateEffectHandler.resolve(gd, entry, effect);

                // First round exiles "Same", "Same" (duplicate â†’ repeat)
                // Second round exiles "Unique1", "Unique2" (no duplicate â†’ stop)
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

                exileTopCardsRepeatOnDuplicateEffectHandler.resolve(gd, entry, effect);

                assertThat(gd.getPlayerExiledCards(player2Id)).isEmpty();
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("library is empty")));
            }
}
