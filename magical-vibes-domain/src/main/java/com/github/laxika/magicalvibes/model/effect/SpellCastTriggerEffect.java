package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.condition.Condition;

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
 * @param intervening                optional source condition checked when the spell is cast and
 *                                   again when the trigger resolves
 * @param nthSpellNumber             if positive, only fire when this is the controller's Nth spell
 *                                   matching {@code spellFilter} this turn
 * @param triggersOnAnyPlayer        when stored on an emblem, fire for every player's spells rather
 *                                   than only the emblem controller's spells
 */
public record SpellCastTriggerEffect(
        CardPredicate spellFilter,
        List<CardEffect> resolvedEffects,
        String manaCost,
        TargetFilter targetFilter,
        StackEntryPredicate castSpellTargetCondition,
        boolean onlyDuringOpponentTurn,
        boolean onlyDuringControllerTurn,
        Condition intervening,
        int nthSpellNumber,
        int minimumSpellNumber,
        boolean triggersOnAnyPlayer
) implements CardEffect {

    public SpellCastTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects,
                                  String manaCost, TargetFilter targetFilter,
                                  StackEntryPredicate castSpellTargetCondition,
                                  boolean onlyDuringOpponentTurn, boolean onlyDuringControllerTurn,
                                  Condition intervening, int nthSpellNumber,
                                  boolean triggersOnAnyPlayer) {
        this(spellFilter, resolvedEffects, manaCost, targetFilter, castSpellTargetCondition,
                onlyDuringOpponentTurn, onlyDuringControllerTurn, intervening, nthSpellNumber, 0,
                triggersOnAnyPlayer);
    }

    public SpellCastTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects,
                                  String manaCost, TargetFilter targetFilter,
                                  StackEntryPredicate castSpellTargetCondition,
                                  boolean onlyDuringOpponentTurn, boolean onlyDuringControllerTurn,
                                  Condition intervening, int nthSpellNumber, int minimumSpellNumber) {
        this(spellFilter, resolvedEffects, manaCost, targetFilter, castSpellTargetCondition,
                onlyDuringOpponentTurn, onlyDuringControllerTurn, intervening, nthSpellNumber,
                minimumSpellNumber, false);
    }

    public SpellCastTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects,
                                  String manaCost, TargetFilter targetFilter,
                                  StackEntryPredicate castSpellTargetCondition,
                                  boolean onlyDuringOpponentTurn, boolean onlyDuringControllerTurn,
                                  Condition intervening, int nthSpellNumber) {
        this(spellFilter, resolvedEffects, manaCost, targetFilter, castSpellTargetCondition,
                onlyDuringOpponentTurn, onlyDuringControllerTurn, intervening, nthSpellNumber, 0, false);
    }

    public SpellCastTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects) {
        this(spellFilter, resolvedEffects, null, null, null, false, false, null, 0, 0, false);
    }

    public SpellCastTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects, String manaCost) {
        this(spellFilter, resolvedEffects, manaCost, null, null, false, false, null, 0, 0, false);
    }

    public SpellCastTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects, String manaCost,
                                  TargetFilter targetFilter) {
        this(spellFilter, resolvedEffects, manaCost, targetFilter, null, false, false, null, 0, 0, false);
    }

    public SpellCastTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects, String manaCost,
                                  TargetFilter targetFilter, StackEntryPredicate castSpellTargetCondition,
                                  boolean onlyDuringOpponentTurn, boolean onlyDuringControllerTurn) {
        this(spellFilter, resolvedEffects, manaCost, targetFilter, castSpellTargetCondition,
                onlyDuringOpponentTurn, onlyDuringControllerTurn, null, 0, 0, false);
    }

    /** Trigger gated on the cast spell's targets (e.g. Repartee — "spell that targets a creature"). */
    public SpellCastTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects,
                                  StackEntryPredicate castSpellTargetCondition) {
        this(spellFilter, resolvedEffects, null, null, castSpellTargetCondition, false, false, null, 0, 0, false);
    }

    /** Targets-gated trigger whose resolved effect itself targets (e.g. Graduation Day). */
    public SpellCastTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects,
                                  TargetFilter targetFilter, StackEntryPredicate castSpellTargetCondition) {
        this(spellFilter, resolvedEffects, null, targetFilter, castSpellTargetCondition, false, false, null, 0, 0, false);
    }

    /** Trigger that only fires when the spell is cast during an opponent's turn (e.g. Glen Elendra Pranksters). */
    public SpellCastTriggerEffect(CardPredicate spellFilter, List<CardEffect> resolvedEffects,
                                  boolean onlyDuringOpponentTurn) {
        this(spellFilter, resolvedEffects, null, null, null, onlyDuringOpponentTurn, false, null, 0, 0, false);
    }

    /** Trigger only on the controller's Nth spell matching {@code spellFilter} this turn. */
    public static SpellCastTriggerEffect nth(int spellNumber, CardPredicate spellFilter,
                                             List<CardEffect> resolvedEffects) {
        return new SpellCastTriggerEffect(spellFilter, resolvedEffects, null, null, null,
                false, false, null, spellNumber, 0, false);
    }

    public static SpellCastTriggerEffect nth(int spellNumber, CardPredicate spellFilter,
                                             List<CardEffect> resolvedEffects, TargetFilter targetFilter) {
        return new SpellCastTriggerEffect(spellFilter, resolvedEffects, null, targetFilter, null,
                false, false, null, spellNumber, 0, false);
    }

    public static SpellCastTriggerEffect atLeast(int spellNumber, CardPredicate spellFilter,
                                                  List<CardEffect> resolvedEffects) {
        return new SpellCastTriggerEffect(spellFilter, resolvedEffects, null, null, null,
                false, false, null, 0, spellNumber, false);
    }

    public static SpellCastTriggerEffect atLeastDuringYourTurn(int spellNumber,
                                                                CardPredicate spellFilter,
                                                                List<CardEffect> resolvedEffects) {
        return new SpellCastTriggerEffect(spellFilter, resolvedEffects, null, null, null,
                false, true, null, 0, spellNumber, false);
    }

    /** Trigger that only fires when the spell is cast during the source controller's own turn (e.g. Eyes of the Wisent). */
    public static SpellCastTriggerEffect duringYourTurn(CardPredicate spellFilter, List<CardEffect> resolvedEffects) {
        return new SpellCastTriggerEffect(spellFilter, resolvedEffects, null, null, null, false, true, null, 0, 0, false);
    }

    /** Spell-cast trigger with a source-relative intervening condition. */
    public static SpellCastTriggerEffect withIntervening(CardPredicate spellFilter,
                                                         List<CardEffect> resolvedEffects,
                                                         Condition intervening) {
        return new SpellCastTriggerEffect(spellFilter, resolvedEffects, null, null, null,
                false, false, intervening, 0, 0, false);
    }

    public static SpellCastTriggerEffect anyPlayer(CardPredicate spellFilter, List<CardEffect> resolvedEffects) {
        return new SpellCastTriggerEffect(spellFilter, resolvedEffects, null, null, null,
                false, false, null, 0, 0, true);
    }
}
