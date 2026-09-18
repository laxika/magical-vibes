package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.PutTargetPlayerGraveyardOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PutTargetPlayerGraveyardOnBottomOfLibraryEffectHandlerTest {

    @Mock
    private GameLogService gameLogService;
    @Mock
    private GraveyardService graveyardService;

    private GameData gameData;
    private UUID playerId;
    private PutTargetPlayerGraveyardOnBottomOfLibraryEffectHandler handler;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "test", playerId, "Player1");
        gameData.playerIds.add(playerId);
        gameData.orderedPlayerIds.add(playerId);
        gameData.playerIdToName.put(playerId, "Player1");
        gameData.playerDecks.put(playerId, Collections.synchronizedList(new ArrayList<>()));
        handler = new PutTargetPlayerGraveyardOnBottomOfLibraryEffectHandler(
                gameLogService, graveyardService);
    }

    @Test
    void movesAllReturnedGraveyardCardsToTheLibraryBottomInRandomOrder() {
        Card first = card("First");
        Card second = card("Second");
        Card existing = card("Existing");
        gameData.playerDecks.get(playerId).add(existing);
        PutTargetPlayerGraveyardOnBottomOfLibraryEffect effect =
                new PutTargetPlayerGraveyardOnBottomOfLibraryEffect();
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card("Endurance"),
                playerId, "Endurance's triggered ability", List.of(effect), playerId, (Zone) null);
        when(graveyardService.takeGraveyardCardsForZoneChange(gameData, playerId))
                .thenReturn(List.of(first, second));

        handler.resolve(gameData, entry, effect);

        assertThat(gameData.playerDecks.get(playerId)).startsWith(existing)
                .hasSize(3)
                .containsExactlyInAnyOrder(existing, first, second);
        verify(gameLogService).append(eq(gameData), any());
    }

    private static Card card(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
