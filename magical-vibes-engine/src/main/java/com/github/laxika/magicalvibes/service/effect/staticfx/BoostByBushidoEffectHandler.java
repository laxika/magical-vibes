package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostByBushidoEffect;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BoostByBushidoEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostByBushidoEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        if (!support.matchesCreatureScope(context, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SAMURAI))) {
            return;
        }
        int bushido = bushidoValue(context.target());
        accumulator.addPower(bushido);
        accumulator.addToughness(bushido);
    }

    private int bushidoValue(Permanent permanent) {
        List<CardEffect> effects = new ArrayList<>(permanent.getCard().getEffects(EffectSlot.ON_BLOCK));
        effects.addAll(permanent.getPersistentTriggeredEffects(EffectSlot.ON_BLOCK));
        effects.addAll(permanent.getTemporaryTriggeredEffects(EffectSlot.ON_BLOCK));
        return effects.stream()
                .filter(BushidoEffect.class::isInstance)
                .map(BushidoEffect.class::cast)
                .mapToInt(BushidoEffect::amount)
                .sum();
    }
}
