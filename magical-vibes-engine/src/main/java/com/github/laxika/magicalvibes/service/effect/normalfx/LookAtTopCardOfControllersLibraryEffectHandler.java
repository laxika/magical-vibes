package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfControllersLibraryEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardOfControllersLibraryEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardOfControllersLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(controllerName + "'s library is empty."));
            return;
        }

        gameLogService.append(gameData,
                GameLog.text(controllerName + " looks at the top card of their library."));
        cardRevealService.revealToPlayer(gameData, controllerId, GameEventFact.RevealZone.LIBRARY,
                List.of(deck.getFirst()), controllerId);
        log.info("Game {} - {} looks at the top card of their library", gameData.id, controllerName);
    }
}
