package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerTopCardsForSacrificedManaValueMayPlayUntilNextEndStepEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPlayerTopCardsForSacrificedManaValueMayPlayUntilNextEndStepEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPlayerTopCardsForSacrificedManaValueMayPlayUntilNextEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileTargetPlayerTopCardsForSacrificedManaValueMayPlayUntilNextEndStepEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        if (targetPlayerId == null || exileEffect.count() <= 0) {
            return;
        }
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        if (deck == null || deck.isEmpty()) {
            return;
        }

        List<String> exiledNames = new ArrayList<>();
        for (int i = 0; i < exileEffect.count() && !deck.isEmpty(); i++) {
            Card topCard = deck.removeFirst();
            exileService.exileCard(gameData, targetPlayerId, topCard);
            exileSupport.grantPlayUntilOwnersNextEndStep(gameData, topCard.getId(), controllerId);
            gameData.exilePlayAnyManaTypeWhileExiled.add(topCard.getId());
            exiledNames.add(topCard.getName());
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetPlayerName = gameData.playerIdToName.get(targetPlayerId);
        gameLogService.append(gameData, GameLog.text(controllerName + " exiles "
                + String.join(", ", exiledNames) + " from " + targetPlayerName
                + "'s library (may play until their next end step)."));
        log.info("Game {} - {} exiles {} cards from {}'s library (may play until their next end step)",
                gameData.id, controllerName, exiledNames.size(), targetPlayerName);
    }
}
