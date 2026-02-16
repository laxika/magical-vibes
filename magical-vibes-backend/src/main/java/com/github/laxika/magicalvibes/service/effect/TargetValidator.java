package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

@FunctionalInterface
public interface TargetValidator {
    void validate(TargetValidationContext context, CardEffect effect);
}
