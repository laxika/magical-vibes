package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardOfOwnLibraryEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfOwnLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTopCardOfOwnLibraryEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String controllerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            String logEntry = controllerName + "'s library is empty — nothing to exile.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            return;
        }

        Card topCard = deck.removeFirst();

        if (e.trackWithSource()) {
            UUID sourcePermanentId = entry.getSourcePermanentId();
            if (sourcePermanentId != null) {
                exileService.exileCard(gameData, controllerId, topCard, sourcePermanentId);
            } else {
                exileService.exileCard(gameData, controllerId, topCard);
            }
        } else {
            exileService.exileCard(gameData, controllerId, topCard);
        }

        String logEntry = controllerName + " exiles " + topCard.getName() + " from the top of their library.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} exiles {} from library top", gameData.id, controllerName, topCard.getName());
    }
}
