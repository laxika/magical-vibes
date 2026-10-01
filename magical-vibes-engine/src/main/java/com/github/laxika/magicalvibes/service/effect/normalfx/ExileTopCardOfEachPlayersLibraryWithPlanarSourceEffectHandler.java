package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachPlayersLibraryWithPlanarSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileTopCardOfEachPlayersLibraryWithPlanarSourceEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfEachPlayersLibraryWithPlanarSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePlanarObject() == null) {
            return;
        }

        UUID sourcePlanarId = entry.getSourcePlanarObject().getId();
        String sourceName = entry.getCard().getName();
        for (UUID playerId : List.copyOf(gameData.orderedPlayerIds)) {
            List<Card> library = gameData.playerDecks.get(playerId);
            if (library == null || library.isEmpty()) {
                continue;
            }

            Card topCard = library.removeFirst();
            exileService.exileCard(gameData, playerId, topCard, sourcePlanarId);
            gameLogService.append(gameData, GameLog.builder()
                    .text(gameData.playerIdToName.get(playerId) + " exiles ")
                    .card(topCard)
                    .text(" from the top of their library with " + sourceName + ".")
                    .build());
        }
    }
}
