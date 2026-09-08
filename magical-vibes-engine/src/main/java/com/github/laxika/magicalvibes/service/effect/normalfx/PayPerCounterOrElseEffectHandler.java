package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PayPerCounterOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "You may pay {@code costPerCounter} for each [counter] on this permanent. If you don't, …"
 * With zero counters the cost is empty and the fallback never fires (no prompt).
 */
@Component
@RequiredArgsConstructor
public class PayPerCounterOrElseEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final ForcedCostOrElseEffectHandler forcedCostOrElseEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayPerCounterOrElseEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PayPerCounterOrElseEffect) effect;
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }

        int counters = self.getCounterCount(e.counterType());
        if (counters <= 0) {
            return;
        }

        String totalCost = e.costPerCounter().repeat(counters);
        if (e.paidEffects().isEmpty()) {
            ForcedCostOrElseEffect payOrElse = new ForcedCostOrElseEffect(
                    new PayManaCost(totalCost),
                    e.elseEffects(),
                    true);
            forcedCostOrElseEffectHandler.resolve(gameData, entry, payOrElse);
            return;
        }

        CardEffect paid = oneOrSequence(e.paidEffects());
        CardEffect unpaid = e.elseEffects().isEmpty() ? null : oneOrSequence(e.elseEffects());
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex >= 0) {
            entry.insertEffectsToResolve(effectIndex + 1, List.of(new MayPayManaEffect(
                    totalCost, paid, "Pay " + totalCost + "?", unpaid)));
        }
    }

    private CardEffect oneOrSequence(List<CardEffect> effects) {
        return effects.size() == 1 ? effects.getFirst() : new SequenceEffect(effects);
    }
}
