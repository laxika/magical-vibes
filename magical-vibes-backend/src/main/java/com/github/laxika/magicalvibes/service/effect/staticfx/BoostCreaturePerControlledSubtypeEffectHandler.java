package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturePerControlledSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BoostCreaturePerControlledSubtypeEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostCreaturePerControlledSubtypeEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostCreaturePerControlledSubtypeEffect) effect;
        if (!support.matchesCreatureScope(context, boost.scope(), null)) {
            return;
        }

        UUID controllerId = support.findControllerId(context.gameData(), context.source());
        if (controllerId == null) {
            return;
        }

        List<Permanent> battlefield = context.gameData().playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        int count = 0;
        for (Permanent permanent : battlefield) {
            if (permanent.getCard().getSubtypes().contains(boost.subtype())) {
                count++;
            }
        }

        accumulator.addPower(count * boost.powerPerSubtype());
        accumulator.addToughness(count * boost.toughnessPerSubtype());
    }
}
