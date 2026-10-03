package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ControlTargetPlayersNextTurnsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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

@ExtendWith(MockitoExtension.class)
class ControlTargetPlayersNextTurnsEffectHandlerTest {

    @Mock
    private GameLogService gameLogService;

    private GameData gameData;
    private UUID firstPlayerId;
    private UUID secondPlayerId;
    private ControlTargetPlayersNextTurnsEffectHandler handler;

    @BeforeEach
    void setUp() {
        firstPlayerId = UUID.randomUUID();
        secondPlayerId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "test", firstPlayerId, "First");
        gameData.playerIds.add(firstPlayerId);
        gameData.playerIds.add(secondPlayerId);
        gameData.orderedPlayerIds.add(firstPlayerId);
        gameData.orderedPlayerIds.add(secondPlayerId);
        gameData.playerIdToName.put(firstPlayerId, "First");
        gameData.playerIdToName.put(secondPlayerId, "Second");
        gameData.playerBattlefields.put(firstPlayerId, Collections.synchronizedList(new ArrayList<>()));
        gameData.playerBattlefields.put(secondPlayerId, Collections.synchronizedList(new ArrayList<>()));
        gameData.playerHands.put(firstPlayerId, Collections.synchronizedList(new ArrayList<>()));
        gameData.playerHands.put(secondPlayerId, Collections.synchronizedList(new ArrayList<>()));
        gameData.playerGraveyards.put(firstPlayerId, Collections.synchronizedList(new ArrayList<>()));
        gameData.playerGraveyards.put(secondPlayerId, Collections.synchronizedList(new ArrayList<>()));
        gameData.playerDecks.put(firstPlayerId, Collections.synchronizedList(new ArrayList<>()));
        gameData.playerDecks.put(secondPlayerId, Collections.synchronizedList(new ArrayList<>()));
        handler = new ControlTargetPlayersNextTurnsEffectHandler(gameLogService);
    }

    @Test
    void schedulesBothDirections() {
        Card card = new Card();
        card.setName("Cruel Entertainment");
        card.setType(CardType.SORCERY);
        StackEntry entry = new StackEntry(
                StackEntryType.SORCERY_SPELL,
                card,
                firstPlayerId,
                card.getName(),
                List.of(new ControlTargetPlayersNextTurnsEffect()),
                0,
                List.of(firstPlayerId, secondPlayerId));

        handler.resolve(gameData, entry, new ControlTargetPlayersNextTurnsEffect());

        assertThat(gameData.pendingTurnControl)
                .containsEntry(firstPlayerId, secondPlayerId)
                .containsEntry(secondPlayerId, firstPlayerId);
    }
}
