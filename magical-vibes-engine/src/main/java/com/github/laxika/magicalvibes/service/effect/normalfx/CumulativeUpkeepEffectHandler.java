package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Cumulative upkeep (CR 702.24): put an age counter, then you may pay the cost for each age counter
 * or sacrifice the permanent. Mana payment is flagged for cumulative-upkeep-only mana; sacrifice
 * costs reuse {@link SacrificeMultiplePermanentsCost} (one matching permanent per age counter).
 */
@Component
@RequiredArgsConstructor
public class CumulativeUpkeepEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final ForcedCostOrElseEffectHandler forcedCostOrElseEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CumulativeUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CumulativeUpkeepEffect) effect;
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }

        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, self, CounterType.AGE, 1);
        int ageCounters = self.getCounterCount(CounterType.AGE);
        if (ageCounters <= 0) {
            return;
        }

        ForcedCostOrElseEffect payOrSacrifice;
        if (e.isSacrificeCost()) {
            payOrSacrifice = new ForcedCostOrElseEffect(
                    new SacrificeMultiplePermanentsCost(ageCounters, e.sacrificeFilter()),
                    List.of(new SacrificeSelfEffect()),
                    true);
        } else {
            String totalCost = e.costPerAge().repeat(ageCounters);
            int totalLife = e.lifePerAge() * ageCounters;
            payOrSacrifice = new ForcedCostOrElseEffect(
                    new PayManaCost(totalCost, null, true, totalLife),
                    List.of(new SacrificeSelfEffect()),
                    true);
        }
        forcedCostOrElseEffectHandler.resolve(gameData, entry, payOrSacrifice);
    }
}
