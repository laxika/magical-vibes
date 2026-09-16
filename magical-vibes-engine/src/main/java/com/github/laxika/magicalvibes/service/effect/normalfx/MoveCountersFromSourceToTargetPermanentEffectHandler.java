package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCountersFromSourceToTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a dynamic counter move from the source permanent onto a permanent target. */
@Component
@RequiredArgsConstructor
public class MoveCountersFromSourceToTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MoveCountersFromSourceToTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var move = (MoveCountersFromSourceToTargetPermanentEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        var targetId = entry.targetsForEffect(effect).stream()
                .findFirst()
                .orElse(entry.getTargetId());
        if (targetId == null) {
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || target == source) {
            return;
        }

        int requested = Math.max(0, amountEvaluationService.evaluate(
                gameData, move.amount(), AmountContext.forStackEntry(entry, source)));
        int amount = Math.min(requested, source.getCounterCount(move.counterType()));
        if (amount <= 0) {
            return;
        }

        int placed = permanentCounterSupport.placeCounterOnPermanent(
                gameData, entry, target, move.counterType(), amount);
        if (placed > 0) {
            permanentCounterSupport.removeCounterFromPermanent(
                    gameData, source, move.counterType(), placed);
        }
    }
}
