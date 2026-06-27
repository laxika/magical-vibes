package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerAttachmentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

@Component
public class BoostSelfPerAttachmentSelfEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostSelfPerAttachmentEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostSelfPerAttachmentEffect) effect;
        final int[] count = {0};
        context.gameData().forEachPermanent((playerId, permanent) -> {
            if (permanent.isAttached() && permanent.getAttachedTo().equals(context.target().getId())) {
                boolean isAura = permanent.getCard().getSubtypes().contains(CardSubtype.AURA);
                boolean isEquipment = permanent.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT);
                if ((boost.countAuras() && isAura) || (boost.countEquipment() && isEquipment)) {
                    count[0]++;
                }
            }
        });
        accumulator.addPower(count[0] * boost.power());
        accumulator.addToughness(count[0] * boost.toughness());
    }
}
