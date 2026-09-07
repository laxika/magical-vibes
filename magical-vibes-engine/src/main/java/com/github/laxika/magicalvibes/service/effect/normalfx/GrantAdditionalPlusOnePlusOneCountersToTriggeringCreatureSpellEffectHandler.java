package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Records additional counters for a triggering creature spell while that spell is still on the
 * stack, so the counters are applied when it enters the battlefield.
 */
@Component
@RequiredArgsConstructor
public class GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect counters =
                (GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect) effect;
        UUID spellCardId = entry.getTriggeringCardId();
        if (spellCardId == null) {
            return;
        }

        StackEntry spellEntry = gameQueryService.findStackEntryByCardId(gameData, spellCardId);
        if (spellEntry == null) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int additionalCounters = Math.max(0, amountEvaluationService.evaluate(
                gameData, counters.amount(), AmountContext.forStackEntry(entry, source)));
        if (additionalCounters == 0) {
            return;
        }

        gameData.spellAdditionalEnterCounters.merge(spellCardId, additionalCounters, Integer::sum);
        gameLogService.append(gameData, GameLog.cardThen(spellEntry.getCard(),
                " enters with " + additionalCounters + " additional +1/+1 counters."));
    }
}
