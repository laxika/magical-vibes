package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllButBottomCardsOfEachLibraryFaceDownEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileAllButBottomCardsOfEachLibraryFaceDownEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAllButBottomCardsOfEachLibraryFaceDownEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int cardsToKeep = Math.max(0,
                ((ExileAllButBottomCardsOfEachLibraryFaceDownEffect) effect).cardsToKeep());
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> library = gameData.playerDecks.get(playerId);
            if (library == null || library.size() <= cardsToKeep) {
                continue;
            }

            int cardsToExile = library.size() - cardsToKeep;
            List<Card> exiledCards = List.copyOf(library.subList(0, cardsToExile));
            library.subList(0, cardsToExile).clear();
            exiledCards.forEach(card -> exileService.exileCardFaceDown(gameData, playerId, card, null));

            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " exiles " + cardsToExile
                            + " card" + (cardsToExile == 1 ? "" : "s")
                            + " from the top of their library face down."));
            log.info("Game {} - {} exiles {} cards from their library face down, leaving {}",
                    gameData.id, playerName, cardsToExile, cardsToKeep);
        }
    }
}
