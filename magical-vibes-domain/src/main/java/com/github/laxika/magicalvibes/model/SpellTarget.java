package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import lombok.Getter;

/**
 * Declares a target for a spell or ability and associates effects with it.
 * <p>
 * Card constructors use the target-first builder pattern:
 * <pre>
 * target(filter).addEffect(EffectSlot.SPELL, effect);
 * </pre>
 * Each {@code target()} call declares a new target slot. Effects added to that
 * slot are resolved using the permanent chosen for that target position.
 *
 * @see Card#target(TargetFilter)
 * @see Card#target(TargetFilter, int, int)
 */
@Getter
public class SpellTarget {

    private final Card card;
    private final TargetFilter filter;
    private final int minTargets;
    private final int maxTargets;
    private final int index;
    /**
     * When true the number of targets scales with the spell's X: the effective max
     * is {@code min(xValue, maxTargets)} and the effective min is {@code min(xValue, minTargets)}.
     * Used by "Destroy X target nonblack creatures"-style spells (Dregs of Sorrow), where
     * {@code maxTargets} acts only as a sanity cap.
     */
    private final boolean xScaled;

    SpellTarget(Card card, TargetFilter filter, int minTargets, int maxTargets, int index) {
        this(card, filter, minTargets, maxTargets, index, false);
    }

    SpellTarget(Card card, TargetFilter filter, int minTargets, int maxTargets, int index, boolean xScaled) {
        this.card = card;
        this.filter = filter;
        this.minTargets = minTargets;
        this.maxTargets = maxTargets;
        this.index = index;
        this.xScaled = xScaled;
    }

    /**
     * Adds an effect to this target slot. The effect is registered on the Card's
     * effect list and mapped to this target's index for resolution.
     */
    public SpellTarget addEffect(EffectSlot slot, CardEffect effect) {
        card.addEffect(slot, effect);
        card.registerEffectTargetIndex(effect, this.index);
        return this;
    }

    /**
     * Adds an effect with a specific trigger mode to this target slot.
     */
    public SpellTarget addEffect(EffectSlot slot, CardEffect effect, TriggerMode triggerMode) {
        card.addEffect(slot, effect, triggerMode);
        card.registerEffectTargetIndex(effect, this.index);
        return this;
    }
}
