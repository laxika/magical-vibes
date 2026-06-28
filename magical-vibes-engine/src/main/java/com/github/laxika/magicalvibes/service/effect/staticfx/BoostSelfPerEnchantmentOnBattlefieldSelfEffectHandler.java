package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerEnchantmentOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

@Component
public class BoostSelfPerEnchantmentOnBattlefieldSelfEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostSelfPerEnchantmentOnBattlefieldEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostSelfPerEnchantmentOnBattlefieldEffect) effect;
        final int[] count = {0};
        context.gameData().forEachPermanent((playerId, permanent) -> {
            if (permanent.getCard().hasType(CardType.ENCHANTMENT)) {
                count[0]++;
            }
        });
        accumulator.addPower(count[0] * boost.powerPerEnchantment());
        accumulator.addToughness(count[0] * boost.toughnessPerEnchantment());
    }
}
