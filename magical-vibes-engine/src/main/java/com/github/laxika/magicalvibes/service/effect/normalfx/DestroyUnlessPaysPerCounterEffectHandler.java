package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroySourcePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyUnlessPaysPerCounterEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "Destroy this permanent unless you pay {@code costPerCounter} for each [counter] on it."
 * With zero counters the cost is empty and the permanent is left alone (no prompt).
 */
@Component
@RequiredArgsConstructor
public class DestroyUnlessPaysPerCounterEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final ForcedCostOrElseEffectHandler forcedCostOrElseEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyUnlessPaysPerCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyUnlessPaysPerCounterEffect) effect;
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }

        int counters = self.getCounterCount(e.counterType());
        if (counters <= 0) {
            return;
        }

        String totalCost = e.costPerCounter().repeat(counters);
        ForcedCostOrElseEffect payOrDestroy = new ForcedCostOrElseEffect(
                new PayManaCost(totalCost),
                List.of(new DestroySourcePermanentEffect()),
                true);
        forcedCostOrElseEffectHandler.resolve(gameData, entry, payOrDestroy);
    }
}
