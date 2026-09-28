package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachPlayersLibraryWithFetchCountersAndPutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Pako's attack trigger. */
@Component
@RequiredArgsConstructor
public class ExileTopCardOfEachPlayersLibraryWithFetchCountersAndPutCountersOnSourceEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfEachPlayersLibraryWithFetchCountersAndPutCountersOnSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        String sourceName = entry.getCard().getName();
        int noncreatureCount = 0;

        for (UUID playerId : List.copyOf(gameData.orderedPlayerIds)) {
            List<Card> library = gameData.playerDecks.get(playerId);
            if (library == null || library.isEmpty()) {
                continue;
            }

            Card card = library.removeFirst();
            gameData.addToExileWithFetchCounter(playerId, card, sourcePermanentId, controllerId);
            if (gameData.exiledCardsWithFetchCounters.contains(card.getId())
                    && !card.hasType(CardType.CREATURE)) {
                noncreatureCount++;
            }

            gameLogService.append(gameData, GameLog.builder()
                    .text(gameData.playerIdToName.get(playerId) + " exiles ")
                    .card(card)
                    .text(" from the top of their library with a fetch counter ("
                            + sourceName + ").")
                    .build());
        }

        if (noncreatureCount == 0 || sourcePermanentId == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source != null) {
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, source, CounterType.PLUS_ONE_PLUS_ONE, noncreatureCount);
        }
    }
}
