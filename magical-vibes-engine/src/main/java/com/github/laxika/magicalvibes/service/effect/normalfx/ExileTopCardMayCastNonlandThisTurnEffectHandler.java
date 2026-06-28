package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayCastNonlandThisTurnEffect;
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
public class ExileTopCardMayCastNonlandThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardMayCastNonlandThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String controllerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            String logEntry = controllerName + "'s library is empty — nothing to exile.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        Card topCard = deck.removeFirst();
        exileService.exileCard(gameData, controllerId, topCard);

        boolean isNonland = !topCard.hasType(CardType.LAND);
        if (isNonland) {
            gameData.exilePlayPermissions.put(topCard.getId(), controllerId);
            gameData.exilePlayPermissionsExpireEndOfTurn.add(topCard.getId());
        }

        String castNote = isNonland ? " (may cast this turn)" : "";
        String logEntry = controllerName + " exiles " + topCard.getName()
                + " from the top of their library" + castNote + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} exiles {} from library top (nonland={})",
                gameData.id, controllerName, topCard.getName(), isNonland);
    }
}
