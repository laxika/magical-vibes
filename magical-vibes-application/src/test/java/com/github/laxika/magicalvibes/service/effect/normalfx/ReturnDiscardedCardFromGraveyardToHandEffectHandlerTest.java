package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ReturnDiscardedCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ReturnDiscardedCardFromGraveyardToHandEffectHandlerTest {

    @Mock private PermanentRemovalService permanentRemovalService;
    @Mock private GameLogService gameLogService;
    @InjectMocks private ReturnDiscardedCardFromGraveyardToHandEffectHandler handler;

    private final UUID controllerId = UUID.randomUUID();
    private final ReturnDiscardedCardFromGraveyardToHandEffect effect =
            new ReturnDiscardedCardFromGraveyardToHandEffect();
    private GameData gameData;
    private Card discarded;
    private StackEntry entry;

    @BeforeEach
    void setUp() {
        gameData = new GameData(UUID.randomUUID(), "test", controllerId, "Controller");
        discarded = new Card();
        discarded.setName("Discarded card");
        gameData.playerHands.put(controllerId, new ArrayList<>());
        gameData.playerGraveyards.put(controllerId, new ArrayList<>(List.of(discarded)));
        Card source = new Card();
        source.setName("Discard trigger source");
        entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source, controllerId,
                "Return discarded card", List.of(effect));
        entry.setTriggeringCardId(discarded.getId());
        entry.setTriggeringCardGraveyardEntryVersion(gameData.markGraveyardEntry(discarded));
    }

    @Test
    void returnsTheOriginalGraveyardObject() {
        handler.resolve(gameData, entry, effect);

        verify(permanentRemovalService).removeCardFromGraveyardById(gameData, discarded.getId());
        assertThat(gameData.playerHands.get(controllerId)).containsExactly(discarded);
    }

    @Test
    void doesNotReturnACardThatLeftAndReenteredTheGraveyard() {
        gameData.markGraveyardEntry(discarded);

        handler.resolve(gameData, entry, effect);

        verifyNoInteractions(permanentRemovalService, gameLogService);
        assertThat(gameData.playerHands.get(controllerId)).isEmpty();
    }

    @Test
    void doesNotReturnACardFromAnotherPlayersGraveyard() {
        gameData.playerGraveyards.get(controllerId).clear();
        gameData.playerGraveyards.put(UUID.randomUUID(), new ArrayList<>(List.of(discarded)));

        handler.resolve(gameData, entry, effect);

        verifyNoInteractions(permanentRemovalService, gameLogService);
        assertThat(gameData.playerHands.get(controllerId)).isEmpty();
    }
}
