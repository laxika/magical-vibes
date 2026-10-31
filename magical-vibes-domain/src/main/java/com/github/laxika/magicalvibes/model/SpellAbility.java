package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CostEffect;

import java.util.List;
import java.util.Objects;

/**
 * A spell ability (CR 113.3a): the instructions a spell follows as it resolves. Its casting costs
 * are not part of it; the card holds those ({@link Card#getCastingCosts()}).
 *
 * @param effects the effects in the order they resolve, at least one. A spell ability doesn't
 *                trigger, so every registration has {@link TriggerMode#NORMAL}. Effects marked
 *                simultaneous happen at the same time as the effect before them, so the first one
 *                can't be.
 * @param targets the spell's target groups, in order: the group at position {@code i} has index
 *                {@code i}. They are the card's first target groups ({@link Card#getTargetGroups()});
 *                groups the card declares after them belong to its other abilities.
 */
public record SpellAbility(List<EffectRegistration> effects, List<TargetGroup> targets) implements Ability {

    public SpellAbility {
        effects = List.copyOf(Objects.requireNonNull(effects, "effects"));
        targets = List.copyOf(Objects.requireNonNull(targets, "targets"));
        if (effects.isEmpty()) {
            throw new IllegalArgumentException("A spell ability needs at least one effect");
        }
        if (effects.getFirst().simultaneousWithPrevious()) {
            throw new IllegalArgumentException("The first effect of a spell ability has no effect before it");
        }
        for (EffectRegistration registration : effects) {
            if (registration.effect() instanceof CostEffect) {
                throw new IllegalArgumentException(registration.effect().getClass().getSimpleName()
                        + " is a casting cost, not part of the spell ability; add it with addCastingCost");
            }
            if (registration.triggerMode() != TriggerMode.NORMAL) {
                throw new IllegalArgumentException("A spell ability doesn't trigger, but "
                        + registration.effect().getClass().getSimpleName() + " has trigger mode "
                        + registration.triggerMode());
            }
        }
        for (int i = 0; i < targets.size(); i++) {
            if (targets.get(i).getIndex() != i) {
                throw new IllegalArgumentException("The spell's target group at position " + i
                        + " has index " + targets.get(i).getIndex());
            }
        }
    }

    /** A spell ability with no target groups. */
    public SpellAbility(List<EffectRegistration> effects) {
        this(effects, List.of());
    }

    /** This spell ability with {@code newEffects} instead of its effects. */
    public SpellAbility withEffects(List<EffectRegistration> newEffects) {
        return new SpellAbility(newEffects, targets);
    }

    /** This spell ability with {@code newTargets} instead of its target groups. */
    public SpellAbility withTargets(List<TargetGroup> newTargets) {
        return new SpellAbility(effects, newTargets);
    }
}
