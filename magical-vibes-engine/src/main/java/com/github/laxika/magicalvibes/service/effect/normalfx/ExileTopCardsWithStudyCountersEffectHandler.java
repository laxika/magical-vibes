package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsWithStudyCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardsWithStudyCountersEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsWithStudyCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTopCardsWithStudyCountersEffect e = (ExileTopCardsWithStudyCountersEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) return;

        int count = Math.max(0, amountEvaluationService.evaluate(gameData, e.count(),
                AmountContext.forStackEntry(entry, null)));
        int exiledCount = Math.min(count, library.size());
        for (int i = 0; i < exiledCount; i++) {
            Card card = library.removeFirst();
            exileService.exileCard(gameData, controllerId, card);
            gameData.exiledCardsWithStudyCounters.add(card.getId());
        }

        if (exiledCount > 0) {
            String controllerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData, GameLog.text(controllerName + " exiles the top "
                    + exiledCount + " card" + (exiledCount == 1 ? "" : "s")
                    + " of their library with study counters."));
            log.info("Game {} - {} exiles {} cards with study counters", gameData.id,
                    controllerName, exiledCount);
        }
    }
}
