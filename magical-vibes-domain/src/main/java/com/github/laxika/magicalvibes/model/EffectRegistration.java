package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

/**
 * Pairs a {@link CardEffect} with its {@link TriggerMode}, controlling how the effect fires.
 */
public record EffectRegistration(CardEffect effect, TriggerMode triggerMode) {

    public EffectRegistration(CardEffect effect) {
        this(effect, TriggerMode.NORMAL);
    }
}
