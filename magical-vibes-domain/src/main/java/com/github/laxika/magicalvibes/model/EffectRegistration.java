package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

/**
 * Pairs a {@link CardEffect} with its {@link TriggerMode}, controlling how the effect fires.
 *
 * @param simultaneousWithPrevious whether the effect happens at the same time as the effect
 *                                 registered before it in the same slot, so the damage both deal is
 *                                 one damage event (see {@link Card#addSimultaneousEffects})
 */
public record EffectRegistration(CardEffect effect, TriggerMode triggerMode, boolean simultaneousWithPrevious) {

    public EffectRegistration(CardEffect effect) {
        this(effect, TriggerMode.NORMAL);
    }

    public EffectRegistration(CardEffect effect, TriggerMode triggerMode) {
        this(effect, triggerMode, false);
    }
}
