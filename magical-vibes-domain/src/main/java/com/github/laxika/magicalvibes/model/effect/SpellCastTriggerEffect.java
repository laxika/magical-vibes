package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

import java.util.List;

/**
 * Generic trigger descriptor for "whenever [someone] casts a spell [matching filter]" abilities.
 * <p>
 * Works in both {@code ON_ANY_PLAYER_CASTS_SPELL} and {@code ON_CONTROLLER_CASTS_SPELL} slots —
 * the EffectSlot determines which loop fires it.
 * <p>
 * When wrapped in {@link MayEffect}, the player is prompted before the effects resolve.
 * If {@code manaCost} is non-null, a "may pay" prompt is shown and the cost must be paid.
 * <p>
 * If {@code targetFilter} is non-null, the resolved effects require targeting and the filter
 * restricts which permanents can be chosen (e.g. "target creature an opponent controls").
 * <p>
 * If {@code castSpellTargetCondition} is non-null, the trigger only fires when the cast spell's
 * own stack entry satisfies the predicate (e.g. "targets a creature" for the Repartee mechanic).
 * This inspects the spell's chosen targets, which {@code spellFilter} (a card-only predicate)
 * cannot express.
 *
 * @param spellFilter               what spells trigger this (null = any spell)
 * @param resolvedEffects           effects to put on the stack when this triggers
 * @param manaCost                  optional mana cost string, e.g. "{1}" (null = free)
 * @param targetFilter              optional target filter for triggered abilities that target (null = no targeting)
 * @param castSpellTargetCondition  optional predicate on the cast spell's stack entry / targets (null = no condition)
 */
public record SpellCastTriggerEffect(
        CardPredicate spellFilter,
        List<CardEffect> resolvedEffects,
        String manaCost,
        TargetFilter targetFilter,
        StackEntryPredicate castSpellTargetCondition
) implements CardEffect {

    public SpellCastTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects) {
        this(spellFilter, resolvedEffects, null, null, null);
    }

    public SpellCastTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects, String manaCost) {
        this(spellFilter, resolvedEffects, manaCost, null, null);
    }

    public SpellCastTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects, String manaCost,
                                  TargetFilter targetFilter) {
        this(spellFilter, resolvedEffects, manaCost, targetFilter, null);
    }

    /** Trigger gated on the cast spell's targets (e.g. Repartee — "spell that targets a creature"). */
    public SpellCastTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects,
                                  StackEntryPredicate castSpellTargetCondition) {
        this(spellFilter, resolvedEffects, null, null, castSpellTargetCondition);
    }

    /** Targets-gated trigger whose resolved effect itself targets (e.g. Graduation Day). */
    public SpellCastTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects,
                                  TargetFilter targetFilter, StackEntryPredicate castSpellTargetCondition) {
        this(spellFilter, resolvedEffects, null, targetFilter, castSpellTargetCondition);
    }
}
