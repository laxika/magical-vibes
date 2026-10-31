package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.Objects;

/**
 * A spell ability (CR 113.3a): the instructions an instant or sorcery spell follows as it resolves.
 *
 * @param effects the effects in the order they resolve, at least one. A spell ability doesn't
 *                trigger, so every registration has {@link TriggerMode#NORMAL}. Effects marked
 *                simultaneous happen at the same time as the effect before them, so the first one
 *                can't be.
 */
public record SpellAbility(List<EffectRegistration> effects) implements Ability {

    public SpellAbility {
        effects = List.copyOf(Objects.requireNonNull(effects, "effects"));
        if (effects.isEmpty()) {
            throw new IllegalArgumentException("A spell ability needs at least one effect");
        }
        if (effects.getFirst().simultaneousWithPrevious()) {
            throw new IllegalArgumentException("The first effect of a spell ability has no effect before it");
        }
        for (EffectRegistration registration : effects) {
            if (registration.triggerMode() != TriggerMode.NORMAL) {
                throw new IllegalArgumentException("A spell ability doesn't trigger, but "
                        + registration.effect().getClass().getSimpleName() + " has trigger mode "
                        + registration.triggerMode());
            }
        }
    }
}
