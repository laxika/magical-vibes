package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.RevokeExilePlayPermissionAtNextUpkeep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayUntilNextUpkeepEffect;
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
public class ExileTopCardMayPlayUntilNextUpkeepEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardMayPlayUntilNextUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String controllerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            String logEntry = controllerName + "'s library is empty — nothing to exile.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            return;
        }

        Card topCard = deck.removeFirst();
        exileService.exileCard(gameData, controllerId, topCard);
        gameData.exilePlayPermissions.put(topCard.getId(), controllerId);
        gameData.queueDelayedAction(new RevokeExilePlayPermissionAtNextUpkeep(
                controllerId, topCard.getId(), entry.getCard()));

        String logEntry = controllerName + " exiles " + topCard.getName()
                + " from the top of their library (may play it until their next upkeep).";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().text(controllerName + " exiles ").card(topCard).text(" from the top of their library (may play it until their next upkeep).").build());
        log.info("Game {} - {} exiles {} from library top (may play until next upkeep)",
                gameData.id, controllerName, topCard.getName());
    }
}
