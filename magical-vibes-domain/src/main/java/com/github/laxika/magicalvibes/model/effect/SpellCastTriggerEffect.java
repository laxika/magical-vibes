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
 * <p>
 * If {@code onlyDuringOpponentTurn} is true, the trigger only fires when the spell is cast on a
 * turn other than the source's controller's (e.g. Glen Elendra Pranksters).
 * <p>
 * If {@code onlyDuringControllerTurn} is true, the trigger only fires when the spell is cast during
 * the source controller's own turn (e.g. Eyes of the Wisent — "an opponent casts a blue spell
 * during your turn"). Use the {@link #duringYourTurn} factory to build these.
 *
 * @param spellFilter               what spells trigger this (null = any spell)
 * @param resolvedEffects           effects to put on the stack when this triggers
 * @param manaCost                  optional mana cost string, e.g. "{1}" (null = free)
 * @param targetFilter              optional target filter for triggered abilities that target (null = no targeting)
 * @param castSpellTargetCondition  optional predicate on the cast spell's stack entry / targets (null = no condition)
 * @param onlyDuringOpponentTurn    only fire when cast during an opponent's turn
 * @param onlyDuringControllerTurn  only fire when cast during the source controller's own turn
 */
public record SpellCastTriggerEffect(
        CardPredicate spellFilter,
        List<CardEffect> resolvedEffects,
        String manaCost,
        TargetFilter targetFilter,
        StackEntryPredicate castSpellTargetCondition,
        boolean onlyDuringOpponentTurn,
        boolean onlyDuringControllerTurn
) implements CardEffect {

    public SpellCastTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects) {
        this(spellFilter, resolvedEffects, null, null, null, false, false);
    }

    public SpellCastTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects, String manaCost) {
        this(spellFilter, resolvedEffects, manaCost, null, null, false, false);
    }

    public SpellCastTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects, String manaCost,
                                  TargetFilter targetFilter) {
        this(spellFilter, resolvedEffects, manaCost, targetFilter, null, false, false);
    }

    /** Trigger gated on the cast spell's targets (e.g. Repartee — "spell that targets a creature"). */
    public SpellCastTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects,
                                  StackEntryPredicate castSpellTargetCondition) {
        this(spellFilter, resolvedEffects, null, null, castSpellTargetCondition, false, false);
    }

    /** Targets-gated trigger whose resolved effect itself targets (e.g. Graduation Day). */
    public SpellCastTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects,
                                  TargetFilter targetFilter, StackEntryPredicate castSpellTargetCondition) {
        this(spellFilter, resolvedEffects, null, targetFilter, castSpellTargetCondition, false, false);
    }

    /** Trigger that only fires when the spell is cast during an opponent's turn (e.g. Glen Elendra Pranksters). */
    public SpellCastTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects,
                                  boolean onlyDuringOpponentTurn) {
        this(spellFilter, resolvedEffects, null, null, null, onlyDuringOpponentTurn, false);
    }

    /** Trigger that only fires when the spell is cast during the source controller's own turn (e.g. Eyes of the Wisent). */
    public static SpellCastTriggerEffect duringYourTurn(CardPredicate spellFilter, List<CardEffect> resolvedEffects) {
        return new SpellCastTriggerEffect(spellFilter, resolvedEffects, null, null, null, false, true);
    }
}
