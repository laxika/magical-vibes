package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextEndStepEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardsMayPlayUntilNextEndStepEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsMayPlayUntilNextEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTopCardsMayPlayUntilNextEndStepEffect exileEffect =
                (ExileTopCardsMayPlayUntilNextEndStepEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        if (deck == null || deck.isEmpty() || exileEffect.count() <= 0) {
            return;
        }

        List<String> exiledNames = new ArrayList<>();
        for (int i = 0; i < exileEffect.count() && !deck.isEmpty(); i++) {
            Card topCard = deck.removeFirst();
            exileService.exileCard(gameData, controllerId, topCard);
            exileSupport.grantPlayUntilOwnersNextEndStep(gameData, topCard.getId(), controllerId);
            exiledNames.add(topCard.getName());
        }

        gameLogService.append(gameData, GameLog.text(controllerName + " exiles "
                + String.join(", ", exiledNames)
                + " from the top of their library (may play until their next end step)."));
        log.info("Game {} - {} exiles {} cards from library top (may play until next end step)",
                gameData.id, controllerName, exiledNames.size());
    }
}
