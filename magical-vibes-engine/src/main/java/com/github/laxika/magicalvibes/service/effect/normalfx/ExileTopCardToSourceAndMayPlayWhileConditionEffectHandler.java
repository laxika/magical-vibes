package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardToSourceAndMayPlayWhileConditionEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Pick Up the Pace's source-linked top-card exile and conditional play permission. The
 * permission ends when the source permanent leaves the battlefield or changes controller.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardToSourceAndMayPlayWhileConditionEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardToSourceAndMayPlayWhileConditionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileTopCardToSourceAndMayPlayWhileConditionEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId();
        boolean sourceOnBattlefield = sourcePermanentId != null
                && gameQueryService.findPermanentById(gameData, sourcePermanentId) != null;

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(controllerName + "'s library is empty — nothing to exile."));
            return;
        }

        Card topCard = deck.removeFirst();
        exileService.exileCard(gameData, controllerId, topCard, sourcePermanentId);
        if (sourceOnBattlefield) {
            gameData.exilePlayPermissions.put(topCard.getId(), controllerId);
            gameData.exilePlayPermissionSourcePermanents.put(topCard.getId(), sourcePermanentId);
            if (exileEffect.permissionCondition() != null) {
                gameData.exilePlayPermissionConditions.put(topCard.getId(), exileEffect.permissionCondition());
            }
        }

        gameLogService.append(gameData, GameLog.builder()
                .text(controllerName + " exiles ").card(topCard)
                .text(" from the top of their library.").build());
        log.info("Game {} - {} exiles {} from library top with source {}",
                gameData.id, controllerName, topCard.getName(), sourcePermanentId);
    }
}
