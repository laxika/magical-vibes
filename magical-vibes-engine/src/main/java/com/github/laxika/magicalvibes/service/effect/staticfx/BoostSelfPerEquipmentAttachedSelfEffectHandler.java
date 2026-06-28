package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerEquipmentAttachedEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

@Component
public class BoostSelfPerEquipmentAttachedSelfEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostSelfPerEquipmentAttachedEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostSelfPerEquipmentAttachedEffect) effect;
        final int[] count = {0};
        context.gameData().forEachPermanent((playerId, permanent) -> {
            if (permanent.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                    && permanent.isAttached()
                    && permanent.getAttachedTo().equals(context.target().getId())) {
                count[0]++;
            }
        });
        accumulator.addPower(count[0] * boost.powerPerEquipment());
        accumulator.addToughness(count[0] * boost.toughnessPerEquipment());
    }
}
