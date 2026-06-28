package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MillByHandSizeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.normalfx.MillByHandSizeEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport;
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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MillByHandSizeEffectHandlerTest {

    @Mock
    private GraveyardService graveyardService;
    @Mock
    private GameBroadcastService gameBroadcastService;
    @Mock
    private PermanentControlSupport permanentControlSupport;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private MillByHandSizeEffectHandler millByHandSizeEffectHandler;

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
        millByHandSizeEffectHandler = new MillByHandSizeEffectHandler(graveyardService, gameBroadcastService);

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
            @DisplayName("Mills cards equal to target player's hand size")
            void millsByHandSize() {
                gd.playerHands.get(player1Id).addAll(List.of(
                        createCard("Bear1"), createCard("Bear2"), createCard("Bear3")));

                MillByHandSizeEffect effect = new MillByHandSizeEffect();
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Dreamborn Muse"),
                        player1Id, "Dreamborn Muse", List.of(effect), 0, player1Id, null);

                millByHandSizeEffectHandler.resolve(gd, entry, effect);

                verify(graveyardService).resolveMillPlayer(gd, player1Id, 3);
            }

            @Test
            @DisplayName("Mills nothing when hand is empty")
            void millsNothingWithEmptyHand() {
                MillByHandSizeEffect effect = new MillByHandSizeEffect();
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Dreamborn Muse"),
                        player1Id, "Dreamborn Muse", List.of(effect), 0, player1Id, null);

                millByHandSizeEffectHandler.resolve(gd, entry, effect);

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

                millByHandSizeEffectHandler.resolve(gd, entry, effect);

                verify(graveyardService).resolveMillPlayer(gd, player1Id, 5);
            }
}
