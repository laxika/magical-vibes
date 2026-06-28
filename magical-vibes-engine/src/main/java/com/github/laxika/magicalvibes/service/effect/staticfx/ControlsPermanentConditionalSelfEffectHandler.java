package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsPermanentConditionalEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ControlsPermanentConditionalSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ControlsPermanentConditionalEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (ControlsPermanentConditionalEffect) effect;
        UUID controllerId = support.findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;
        List<Permanent> battlefield = context.gameData().playerBattlefields.get(controllerId);
        if (battlefield == null) return;
        boolean found = battlefield.stream()
                .anyMatch(p -> support.matchesStaticFilter(p, conditional.filter()));
        if (found) {
            support.applySelfOnlyConditionalStaticEffect(context, conditional.wrapped(), accumulator);
        }
    }
}
