package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerByChargeCountersEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.normalfx.MillTargetPlayerByChargeCountersEffectHandler;
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
class MillTargetPlayerByChargeCountersEffectHandlerTest {

    @Mock
    private GraveyardService graveyardService;
    @Mock
    private GameBroadcastService gameBroadcastService;
    @Mock
    private PermanentControlSupport permanentControlSupport;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private MillTargetPlayerByChargeCountersEffectHandler millTargetPlayerByChargeCountersEffectHandler;

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
        millTargetPlayerByChargeCountersEffectHandler =
                new MillTargetPlayerByChargeCountersEffectHandler(graveyardService, gameBroadcastService);

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
            @DisplayName("Mills nothing with zero charge counters")
            void millsNothingWithZeroCounters() {
                MillTargetPlayerByChargeCountersEffect effect = new MillTargetPlayerByChargeCountersEffect();
                StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Grindclock"),
                        player1Id, "Grindclock", List.of(effect), 0, player2Id, null);

                millTargetPlayerByChargeCountersEffectHandler.resolve(gd, entry, effect);

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

                millTargetPlayerByChargeCountersEffectHandler.resolve(gd, entry, effect);

                verify(graveyardService).resolveMillPlayer(gd, player2Id, 10);
            }
}
