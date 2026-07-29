package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromChosenColorEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * "Enchanted creature has protection from the chosen color" (Ward of Lights). The colour is read at
 * evaluation time from the Aura's own {@code chosenColor}, so it tracks a later re-choice.
 * The self-scoped shape (Voice of All) is seeded directly by the layer system and never reaches here.
 */
@Component
public class ProtectionFromChosenColorEffectHandler implements StaticEffectHandlerBean {
    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ProtectionFromChosenColorEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var protection = (ProtectionFromChosenColorEffect) effect;
        if (protection.scope() != GrantScope.ENCHANTED_CREATURE) {
            return;
        }
        CardColor chosen = context.source().getChosenColor();
        if (chosen != null && context.source().isAttached()
                && context.source().getAttachedTo().equals(context.target().getId())) {
            accumulator.addProtectionColors(Set.of(chosen));
        }
    }
}
