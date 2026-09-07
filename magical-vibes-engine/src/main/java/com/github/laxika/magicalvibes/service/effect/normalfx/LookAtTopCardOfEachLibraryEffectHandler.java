package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfEachLibraryEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardOfEachLibraryEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardOfEachLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        String controllerName = gameData.playerIdToName.get(entry.getControllerId());
        gameLogService.append(gameData,
                GameLog.text(controllerName + " looks at the top card of each player's library."));

        for (var playerId : gameData.orderedPlayerIds) {
            List<Card> deck = gameData.playerDecks.get(playerId);
            if (deck == null || deck.isEmpty()) {
                continue;
            }
            cardRevealService.revealToPlayer(gameData, playerId, GameEventFact.RevealZone.LIBRARY,
                    List.of(deck.getFirst()), entry.getControllerId());
        }

        log.info("Game {} - {} looks at the top card of each player's library",
                gameData.id, controllerName);
    }
}
