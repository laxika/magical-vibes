package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllButBottomCardOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileAllButBottomCardOfTargetLibraryEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAllButBottomCardOfTargetLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getTargetId();
        if (playerId == null) {
            return;
        }
        List<Card> library = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);
        if (library == null || library.size() <= 1) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " exiles no cards from their library; the bottom card remains."));
            return;
        }

        int cardsToExile = library.size() - 1;
        List<Card> exiledCards = List.copyOf(library.subList(0, cardsToExile));
        library.subList(0, cardsToExile).clear();
        exiledCards.forEach(card -> exileService.exileCard(gameData, playerId, card));
        gameLogService.append(gameData, GameLog.text(
                playerName + " exiles all but the bottom card of their library ("
                        + cardsToExile + " cards)."));
        log.info("Game {} - {} exiles {} cards from their library, leaving the bottom card",
                gameData.id, playerName, cardsToExile);
    }
}
