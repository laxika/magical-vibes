package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MillControllerEffect;
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

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MillControllerEffectHandlerTest {

    @Mock
    private GraveyardService graveyardService;
    @Mock
    private GameBroadcastService gameBroadcastService;
    @Mock
    private PermanentControlSupport permanentControlSupport;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private MillControllerEffectHandler millControllerEffectHandler;

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
        millControllerEffectHandler = new MillControllerEffectHandler(graveyardService);

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
            @DisplayName("Mills controller for fixed count")
            void millsControllerFixedCount() {
                MillControllerEffect effect = new MillControllerEffect(4);
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Armored Skaab"),
                        player1Id, "Armored Skaab", List.of(effect));

                millControllerEffectHandler.resolve(gd, entry, effect);

                verify(graveyardService).resolveMillPlayer(gd, player1Id, 4);
            }
}
