package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTargetSpellAndPlotEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves {@link ExileTargetSpellAndPlotEffect}. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetSpellAndPlotEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final StateTriggerService stateTriggerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetSpellAndPlotEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry target = gameData.stack.stream()
                .filter(stackEntry -> stackEntry.getTargetableId().equals(targetCardId))
                .findFirst()
                .orElse(null);
        if (target == null) {
            log.info("Game {} - {}'s plot target is no longer on the stack",
                    gameData.id, entry.getCard().getName());
            return;
        }

        gameData.stack.remove(target);
        stateTriggerService.cleanupResolvedStateTrigger(gameData, target);

        if (target.isCopy()) {
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " (a copy) ceases to exist."));
            return;
        }

        Card card = target.getCard();
        exileService.exileCard(gameData, target.getOwnerId(), card);
        gameData.plottedCardIds.add(card.getId());
        gameData.grantExilePlayPermission(card.getId(), target.getOwnerId(), Duration.PERMANENT);
        gameData.setExilePlayWithoutPayingManaCost(card.getId(), true);
        ActionEvents.plotted(gameData, target.getOwnerId(), card);

        gameLogService.append(gameData,
                GameLog.cardTextCard(card, " becomes plotted by ", entry.getCard(), "."));
        log.info("Game {} - {} exiles and plots the spell {}", gameData.id,
                entry.getCard().getName(), card.getName());
    }
}
