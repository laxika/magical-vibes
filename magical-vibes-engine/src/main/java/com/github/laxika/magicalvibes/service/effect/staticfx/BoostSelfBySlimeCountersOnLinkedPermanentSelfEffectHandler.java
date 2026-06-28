package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostSelfBySlimeCountersOnLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoostSelfBySlimeCountersOnLinkedPermanentSelfEffectHandler implements StaticEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostSelfBySlimeCountersOnLinkedPermanentEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostSelfBySlimeCountersOnLinkedPermanentEffect) effect;
        Permanent linked = gameQueryService.findPermanentById(context.gameData(), boost.linkedPermanentId());
        int slimeCount = (linked != null) ? linked.getCounterCount(CounterType.SLIME) : 0;
        accumulator.addPower(slimeCount);
        accumulator.addToughness(slimeCount);
    }
}
