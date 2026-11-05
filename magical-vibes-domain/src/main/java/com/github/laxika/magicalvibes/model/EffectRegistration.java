package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

/**
 * One effect of an ability.
 *
 * @param simultaneousWithPrevious whether the effect happens at the same time as the effect
 *                                 registered before it in the same ability, so the damage both deal is
 *                                 one damage event (see {@link Card#addSimultaneousEffects})
 */
public record EffectRegistration(CardEffect effect, boolean simultaneousWithPrevious) {

    public EffectRegistration(CardEffect effect) {
        this(effect, false);
    }
}
