package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayUntilAnotherEffect;
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
public class ExileTopCardMayPlayUntilAnotherEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardMayPlayUntilAnotherEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            return;
        }

        Card topCard = deck.removeFirst();
        UUID previousCardId = gameData.exilePlayPermissionSourceCards.put(sourcePermanentId, topCard.getId());
        if (previousCardId != null) {
            gameData.exilePlayPermissions.remove(previousCardId);
            gameData.exilePlayPermissionsExpireEndOfTurn.remove(previousCardId);
            gameData.exilePlayPermissionsExpireAtTurnEnd.remove(previousCardId);
        }

        exileService.exileCard(gameData, controllerId, topCard);
        gameData.exilePlayPermissions.put(topCard.getId(), controllerId);

        String controllerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.text(controllerName + " exiles " + topCard.getName()
                + " from the top of their library and may play it until they exile another card."));
        log.info("Game {} - {} exiles {} from library top and may play it until another card is exiled",
                gameData.id, controllerName, topCard.getName());
    }
}
