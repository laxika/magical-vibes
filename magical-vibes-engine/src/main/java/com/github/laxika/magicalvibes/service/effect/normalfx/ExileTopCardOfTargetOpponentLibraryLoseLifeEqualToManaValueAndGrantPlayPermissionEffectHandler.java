package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfTargetOpponentLibraryLoseLifeEqualToManaValueAndGrantPlayPermissionEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Thought-String Analyst's upkeep trigger. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardOfTargetOpponentLibraryLoseLifeEqualToManaValueAndGrantPlayPermissionEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final LifeSupport lifeSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfTargetOpponentLibraryLoseLifeEqualToManaValueAndGrantPlayPermissionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        List<Card> library = targetPlayerId == null ? null : gameData.playerDecks.get(targetPlayerId);
        if (controllerId == null || targetPlayerId == null || library == null || library.isEmpty()) {
            return;
        }

        Card topCard = library.removeFirst();
        int manaValue = topCard.getManaValue();
        exileService.exileCardFaceDown(gameData, targetPlayerId, topCard, null, controllerId);
        gameData.exilePlayPermissions.put(topCard.getId(), controllerId);
        gameData.exilePlayAnyManaTypeWhileExiled.add(topCard.getId());

        if (manaValue > 0) {
            lifeSupport.applyLifeLoss(gameData, controllerId, manaValue, entry.getCard().getName());
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        gameLogService.append(gameData, GameLog.text(
                controllerName + " looks at and exiles a card from " + targetName
                        + "'s library face down."));
        log.info("Game {} - {} looks at and exiles a card from {}'s library face down; "
                        + "the card remains playable while exiled",
                gameData.id, controllerName, targetName);
    }
}
