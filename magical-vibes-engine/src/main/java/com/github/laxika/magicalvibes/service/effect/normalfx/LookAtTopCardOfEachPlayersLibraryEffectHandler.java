package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfEachPlayersLibraryEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LookAtTopCardOfEachPlayersLibraryEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardOfEachPlayersLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        gameLogService.append(gameData, GameLog.text(
                controllerName + " looks at the top card of each player's library (" + sourceName + ")."));

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> deck = gameData.playerDecks.get(playerId);
            if (deck != null && !deck.isEmpty()) {
                cardRevealService.revealToPlayer(
                        gameData, playerId, GameEventFact.RevealZone.LIBRARY,
                        List.of(deck.getFirst()), controllerId);
            }
        }
    }
}
