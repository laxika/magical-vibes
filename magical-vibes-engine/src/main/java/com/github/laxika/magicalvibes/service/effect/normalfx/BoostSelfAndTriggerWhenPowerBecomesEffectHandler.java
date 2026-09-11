package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostSelfAndTriggerWhenPowerBecomesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BoostSelfAndTriggerWhenPowerBecomesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final BoostSelfEffectHandler boostSelfEffectHandler;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostSelfAndTriggerWhenPowerBecomesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (BoostSelfAndTriggerWhenPowerBecomesEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId() != null
                ? entry.getSourcePermanentId()
                : entry.getTargetId();
        Permanent source = sourcePermanentId == null
                ? null
                : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            return;
        }

        int powerBefore = gameQueryService.getEffectivePower(gameData, source);
        boostSelfEffectHandler.resolve(gameData, entry, e.boost());

        source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            return;
        }

        int powerAfter = gameQueryService.getEffectivePower(gameData, source);
        if (powerBefore == e.powerThreshold() || powerAfter != e.powerThreshold()) {
            return;
        }

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s triggered ability triggers."));
        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                entry.getCard(), entry.getControllerId(), List.of(e.triggeredEffect()),
                false, null, 0, sourcePermanentId));
        triggerCollectionService.processNextSpellTargetTrigger(gameData);
    }
}
