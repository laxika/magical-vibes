package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalCountersToCastSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves the additional-counter rider for the creature spell that caused a trigger.
 */
@Slf4j
@Component
public class GrantAdditionalCountersToCastSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    public GrantAdditionalCountersToCastSpellEffectHandler(GameLogService gameLogService,
                                                           GameQueryService gameQueryService,
                                                           AmountEvaluationService amountEvaluationService) {
        this.gameLogService = gameLogService;
        this.gameQueryService = gameQueryService;
        this.amountEvaluationService = amountEvaluationService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantAdditionalCountersToCastSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID spellCardId = entry.getTriggeringCardId();
        if (spellCardId == null) {
            return;
        }

        boolean spellStillOnStack = gameData.stack.stream()
                .anyMatch(stackEntry -> stackEntry.getCard() != null
                        && spellCardId.equals(stackEntry.getTargetableId()));
        if (!spellStillOnStack) {
            return;
        }

        GrantAdditionalCountersToCastSpellEffect counterEffect =
                (GrantAdditionalCountersToCastSpellEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int additionalCounters = counterEffect.amount() == null
                ? gameData.getSpellCastColorsSpent(spellCardId).size()
                : amountEvaluationService.evaluate(gameData, counterEffect.amount(),
                        AmountContext.forStackEntry(entry, source));
        if (additionalCounters <= 0) {
            return;
        }

        gameData.spellAdditionalEnterCounters.merge(spellCardId, additionalCounters, Integer::sum);
        gameLogService.append(gameData, GameLog.text(
                "The triggering creature enters with " + additionalCounters
                        + " additional +1/+1 counter(s)."));
        log.info("Game {} - triggering creature spell receives {} additional +1/+1 counter(s)",
                gameData.id, additionalCounters);
    }
}
