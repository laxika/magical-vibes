package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

/**
 * Binds effects to a target group a card constructor has just declared. The group itself is a
 * {@link TargetGroup} value on the card; this builder only knows the card and the group's index.
 * <p>
 * Card constructors use the target-first builder pattern:
 * <pre>
 * target(filter).addSpellEffect(effect);
 * target(filter).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, effect);
 * </pre>
 * Each {@code target()} call declares a new target group. Effects added through it are resolved
 * using the objects chosen for that group.
 *
 * @see Card#target(TargetFilter)
 * @see Card#target(TargetFilter, int, int)
 */
public final class SpellTarget {

    /** Effective maximum for an "any number of targets" group; large enough to never constrain play. */
    public static final int ANY_NUMBER_OF_TARGETS = TargetGroup.ANY_NUMBER_OF_TARGETS;

    private final Card card;
    private final int index;

    SpellTarget(Card card, int index) {
        this.card = card;
        this.index = index;
    }

    /** The index of the group this builder binds effects to. */
    public int getIndex() {
        return index;
    }

    /** The group this builder binds effects to, as the card holds it now. */
    public TargetGroup group() {
        return card.getTargetGroups().get(index);
    }

    /** Marks this group as part of the mode {@code scope}, whose targets must all be different objects. */
    public SpellTarget inDistinctScope(int scope) {
        card.setTargetGroupDistinctScope(index, scope);
        return this;
    }

    /** Adds an effect to the card's spell ability and binds it to this group. */
    public SpellTarget addSpellEffect(CardEffect effect) {
        card.addSpellEffect(effect);
        card.registerEffectTargetIndex(effect, this.index);
        return this;
    }

    /**
     * Adds a static ability made of {@code effect} and binds it to this group, as an Aura's enchant
     * ability defines the Aura spell's target (CR 303.4a).
     */
    public SpellTarget addStaticEffect(CardEffect effect) {
        card.addStaticEffect(effect);
        card.registerEffectTargetIndex(effect, this.index);
        return this;
    }

    /**
     * Adds an effect to {@code slot} and binds it to this group. The effect is registered on the
     * card's abilities and mapped to this group's index for resolution.
     */
    public SpellTarget addEffect(EffectSlot slot, CardEffect effect) {
        card.addEffect(slot, effect);
        card.registerEffectTargetIndex(effect, this.index);
        return this;
    }

    /**
     * Binds an effect that is already registered through another target group to this group as well,
     * so one effect instance resolves against the targets of several groups at once (Don and Leo
     * exiles its artifact and creature targets together before returning them).
     */
    public SpellTarget bindEffect(CardEffect effect) {
        card.registerEffectTargetIndex(effect, this.index);
        return this;
    }

    /**
     * Adds an effect with a specific trigger mode to this target group.
     */
    public SpellTarget addEffect(EffectSlot slot, CardEffect effect, TriggerMode triggerMode) {
        card.addEffect(slot, effect, triggerMode);
        card.registerEffectTargetIndex(effect, this.index);
        return this;
    }
}
