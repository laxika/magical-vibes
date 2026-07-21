package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesOnlyLandEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Legacy accumulator contribution for {@link EnchantedPermanentBecomesOnlyLandEffect}.
 * The layer-4 pass owns the CharacteristicState mutation; this handler records the
 * type-replacing land grant for StaticBonus / view assembly via L4Contribution replay.
 */
@Component
@RequiredArgsConstructor
public class EnchantedPermanentBecomesOnlyLandEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedPermanentBecomesOnlyLandEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        if (support.matchesCreatureScope(context, GrantScope.ENCHANTED_PERMANENT, null)) {
            accumulator.setCardTypeOverriding(true);
            accumulator.addGrantedCardType(CardType.LAND);
        }
    }
}
