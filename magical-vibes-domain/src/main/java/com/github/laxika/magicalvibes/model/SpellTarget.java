package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;

/**
 * Binds effects to a target group a card constructor has just declared. The group itself is a
 * {@link TargetGroup} value on the card; this builder only knows the card and the group's index.
 * <p>
 * Card constructors use the target-first builder pattern:
 * <pre>
 * target(filter).addSpellEffect(effect);
 * target(filter).addTrigger(Triggers.enters(ObjectRelation.THIS), effect);
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
     * Adds a triggered ability with {@code trigger} and {@code effects} ({@link Card#addTrigger}) and binds the
     * effects to this group: "when this creature dies, it deals 2 damage to target creature".
     */
    public SpellTarget addTrigger(Trigger trigger, CardEffect... effects) {
        card.addTrigger(trigger, effects);
        for (CardEffect effect : effects) {
            card.registerEffectTargetIndex(effect, this.index);
        }
        return this;
    }

    /**
     * Adds a triggered ability with one trigger condition and {@code options} ({@link Card#addTrigger}) and binds
     * the effects to this group.
     */
    public SpellTarget addTrigger(TriggerCondition condition, TriggerOptions options, CardEffect... effects) {
        card.addTrigger(condition, options, effects);
        for (CardEffect effect : effects) {
            card.registerEffectTargetIndex(effect, this.index);
        }
        return this;
    }

    /**
     * Adds a triggered ability with {@code trigger}, functioning from its default zones, and {@code options}
     * ({@link Card#addTrigger(Trigger, TriggerOptions, CardEffect...)}) and binds the effects to this group.
     */
    public SpellTarget addTrigger(Trigger trigger, TriggerOptions options, CardEffect... effects) {
        return addTrigger(new TriggerCondition(trigger), options, effects);
    }

    /**
     * Adds {@code ability} ({@link Card#addTriggeredAbility}) and binds its effects to this group: one ability with
     * several trigger conditions, "whenever this creature or another creature you control dies, if it had counters
     * on it, put those counters on up to one target creature".
     */
    public SpellTarget addTriggeredAbility(TriggeredAbility ability) {
        card.addTriggeredAbility(ability);
        for (EffectRegistration registration : ability.effects()) {
            card.registerEffectTargetIndex(registration.effect(), this.index);
        }
        return this;
    }

    /**
     * Adds a triggered ability whose registrations carry {@code triggerMode} ({@link Card#addTrigger(Trigger,
     * TriggerMode, CardEffect...)}) and binds the effects to this group.
     */
    public SpellTarget addTrigger(Trigger trigger, TriggerMode triggerMode, CardEffect... effects) {
        card.addTrigger(trigger, triggerMode, effects);
        for (CardEffect effect : effects) {
            card.registerEffectTargetIndex(effect, this.index);
        }
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
