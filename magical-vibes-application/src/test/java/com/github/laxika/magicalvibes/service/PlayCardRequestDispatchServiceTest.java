package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Routing tests for the single shared {@link PlayCardRequest} → {@link GameService} translation.
 * These lock the pass-through of every request field so a newly added field that is not threaded
 * through shows up as a test change here, not as a silent engine rejection in one adapter.
 */
@ExtendWith(MockitoExtension.class)
class PlayCardRequestDispatchServiceTest {

    @Mock private GameService gameService;

    private PlayCardRequestDispatchService dispatchService;

    private GameData gameData;
    private Player player;

    @BeforeEach
    void setUp() {
        dispatchService = new PlayCardRequestDispatchService(gameService);
        gameData = new GameData(UUID.randomUUID(), "test-game", UUID.randomUUID(), "creator");
        player = new Player(UUID.randomUUID(), "tester");
    }

    /** All-null request except the fields a plain cast uses. */
    private static PlayCardRequest plainCast(int cardIndex) {
        return new PlayCardRequest(cardIndex, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    @Test
    @DisplayName("Plain cast routes to the fullest playCard overload with defaults filled")
    void plainCastRoutesToFullOverload() {
        dispatchService.dispatch(gameData, player, plainCast(3));

        verify(gameService).playCard(eq(gameData), eq(player), eq(3), isNull(), isNull(), isNull(),
                eq(List.of()), eq(List.of()), eq(false), isNull(), isNull(), isNull(),
                isNull(), isNull(), eq(false), isNull(), isNull(), isNull());
        verifyNoMoreInteractions(gameService);
    }

    @Test
    @DisplayName("Every cast-shaping field of the request is passed through to playCard")
    void allFieldsPassedThrough() {
        UUID targetId = UUID.randomUUID();
        UUID extraTarget = UUID.randomUUID();
        UUID convoke = UUID.randomUUID();
        UUID sacrifice = UUID.randomUUID();
        UUID altSacrifice = UUID.randomUUID();
        UUID imposedSacrifice = UUID.randomUUID();
        Map<UUID, Integer> damage = Map.of(targetId, 2);

        PlayCardRequest request = new PlayCardRequest(1, 4, targetId, damage,
                List.of(extraTarget), List.of(convoke), true,
                sacrifice, 2, null,
                List.of(altSacrifice), null,
                5, List.of(6, 7),
                true, null, null,
                8, List.of(9, 10), List.of(imposedSacrifice));

        dispatchService.dispatch(gameData, player, request);

        verify(gameService).playCard(eq(gameData), eq(player), eq(1), eq(4), eq(targetId), eq(damage),
                eq(List.of(extraTarget)), eq(List.of(convoke)), eq(true), eq(sacrifice), eq(2),
                eq(List.of(altSacrifice)), eq(5), eq(List.of(6, 7)), eq(true), eq(8),
                eq(List.of(9, 10)), eq(List.of(imposedSacrifice)));
        verifyNoMoreInteractions(gameService);
    }

    @Test
    @DisplayName("Empty cost lists mean 'cost not used' and are normalized to null")
    void emptyCostListsNormalizedToNull() {
        PlayCardRequest request = new PlayCardRequest(0, null, null, null, null, null, null,
                null, null, null, List.of(), null, null, List.of(), null, null, null, null,
                List.of(), List.of());

        dispatchService.dispatch(gameData, player, request);

        verify(gameService).playCard(eq(gameData), eq(player), eq(0), isNull(), isNull(), isNull(),
                eq(List.of()), eq(List.of()), eq(false), isNull(), isNull(), isNull(),
                isNull(), isNull(), eq(false), isNull(), isNull(), isNull());
        verifyNoMoreInteractions(gameService);
    }

    @Test
    @DisplayName("Flashback cast passes retrace discard and tap payments through")
    void flashbackPassesRetraceAndTapPayments() {
        UUID targetId = UUID.randomUUID();
        UUID tapPayment = UUID.randomUUID();

        PlayCardRequest request = new PlayCardRequest(2, 1, targetId, null,
                null, null, null, null, null, null,
                List.of(tapPayment), true,
                null, List.of(4), null, null, "CREATURE", 3, null, null);

        dispatchService.dispatch(gameData, player, request);

        verify(gameService).playFlashbackSpell(eq(gameData), eq(player), eq(2), eq(1), eq(targetId),
                eq(List.of()), eq(List.of(4)), eq(CardType.CREATURE), eq(List.of(tapPayment)), eq(3));
        verifyNoMoreInteractions(gameService);
    }

    @Test
    @DisplayName("Cast from library top routes to playCardFromLibraryTop")
    void fromLibraryTopRoutes() {
        PlayCardRequest request = new PlayCardRequest(0, 2, null, null, null, null, null,
                null, null, null, null, null, null, null, null, true, null, null, null, null);

        dispatchService.dispatch(gameData, player, request);

        verify(gameService).playCardFromLibraryTop(eq(gameData), eq(player), eq(2), isNull());
        verifyNoMoreInteractions(gameService);
    }

    @Test
    @DisplayName("Cast from exile routes to playCardFromExile")
    void fromExileRoutes() {
        UUID exileCardId = UUID.randomUUID();
        PlayCardRequest request = new PlayCardRequest(0, null, null, null, null, null, null,
                null, null, exileCardId, null, null, null, null, null, null, null, null, null, null);

        dispatchService.dispatch(gameData, player, request);

        verify(gameService).playCardFromExile(eq(gameData), eq(player), eq(exileCardId), isNull(), isNull());
        verifyNoMoreInteractions(gameService);
    }
}
