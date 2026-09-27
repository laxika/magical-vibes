package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

/**
 * When resolved, registers a delayed triggered ability for the rest of the turn or through the
 * beginning of the controller's next turn: "Until end of turn, whenever you cast a [filter] spell,
 * [resolvedEffects]." Registered by Mountain Titan and Nightmares and Daydreams.
 *
 * <p>The trigger belongs to the resolving controller and normally remains tied to the source
 * permanent, so it stops firing if that permanent leaves the battlefield. Set
 * {@code sourceMustRemainOnBattlefield} to false for delayed abilities that continue after their
 * source leaves. Unless {@code oneShot} is true, activating the granting ability several times in a
 * turn registers several independent triggers, each of which fires on every matching spell.
 *
 * @param spellFilter                  which cast spells fire the trigger; {@code null} = any spell
 * @param stackEntryFilter             optional filter evaluated against the cast stack entry
 * @param resolvedEffects              effects put on the stack when it fires
 * @param sourceMustRemainOnBattlefield whether the source permanent must still be on the battlefield
 * @param targetFilter                 optional permanent/player target filter used when the delayed
 *                                     trigger goes on the stack
 * @param untilNextTurn                whether the registration lasts through the controller's
 *                                     next turn's beginning instead of through turn cleanup
 * @param persistsUntilConsumed         whether a one-shot registration remains until it fires
 */
public record RegisterDelayedControllerSpellCastTriggerEffect(CardPredicate spellFilter,
                                                               StackEntryPredicate stackEntryFilter,
                                                               List<CardEffect> resolvedEffects,
                                                               boolean oneShot,
                                                               boolean sourceMustRemainOnBattlefield,
                                                               TargetFilter targetFilter,
                                                               boolean untilNextTurn,
                                                               boolean persistsUntilConsumed)
        implements CardEffect {

    public RegisterDelayedControllerSpellCastTriggerEffect(CardPredicate spellFilter,
                                                            StackEntryPredicate stackEntryFilter,
                                                            List<CardEffect> resolvedEffects,
                                                            boolean oneShot,
                                                            boolean sourceMustRemainOnBattlefield,
                                                            TargetFilter targetFilter,
                                                            boolean untilNextTurn) {
        this(spellFilter, stackEntryFilter, resolvedEffects, oneShot,
                sourceMustRemainOnBattlefield, targetFilter, untilNextTurn, false);
    }

    public RegisterDelayedControllerSpellCastTriggerEffect(CardPredicate spellFilter,
                                                            List<CardEffect> resolvedEffects) {
        this(spellFilter, null, resolvedEffects, false, true, null, false);
    }

    public RegisterDelayedControllerSpellCastTriggerEffect(CardPredicate spellFilter,
                                                            List<CardEffect> resolvedEffects,
                                                            boolean sourceMustRemainOnBattlefield) {
        this(spellFilter, null, resolvedEffects, false, sourceMustRemainOnBattlefield, null, false);
    }

    public RegisterDelayedControllerSpellCastTriggerEffect(CardPredicate spellFilter,
                                                            List<CardEffect> resolvedEffects,
                                                            boolean sourceMustRemainOnBattlefield,
                                                            TargetFilter targetFilter) {
        this(spellFilter, null, resolvedEffects, false, sourceMustRemainOnBattlefield, targetFilter, false);
    }

    public RegisterDelayedControllerSpellCastTriggerEffect(CardPredicate spellFilter,
                                                            List<CardEffect> resolvedEffects,
                                                            boolean oneShot,
                                                            boolean sourceMustRemainOnBattlefield) {
        this(spellFilter, null, resolvedEffects, oneShot, sourceMustRemainOnBattlefield, null, false);
    }

    public RegisterDelayedControllerSpellCastTriggerEffect(CardPredicate spellFilter,
                                                            StackEntryPredicate stackEntryFilter,
                                                            List<CardEffect> resolvedEffects,
                                                            boolean oneShot,
                                                            boolean sourceMustRemainOnBattlefield,
                                                            TargetFilter targetFilter) {
        this(spellFilter, stackEntryFilter, resolvedEffects, oneShot,
                sourceMustRemainOnBattlefield, targetFilter, false);
    }

    public static RegisterDelayedControllerSpellCastTriggerEffect withStackEntryFilter(
            StackEntryPredicate stackEntryFilter, List<CardEffect> resolvedEffects,
            boolean sourceMustRemainOnBattlefield) {
        return new RegisterDelayedControllerSpellCastTriggerEffect(
                null, stackEntryFilter, resolvedEffects, false, sourceMustRemainOnBattlefield, null, false);
    }

    public static RegisterDelayedControllerSpellCastTriggerEffect withStackEntryFilter(
            StackEntryPredicate stackEntryFilter, List<CardEffect> resolvedEffects,
            boolean oneShot, boolean sourceMustRemainOnBattlefield) {
        return new RegisterDelayedControllerSpellCastTriggerEffect(
                null, stackEntryFilter, resolvedEffects, oneShot, sourceMustRemainOnBattlefield, null, false);
    }

    /** Registers a controller spell-cast trigger through the beginning of that controller's next turn. */
    public static RegisterDelayedControllerSpellCastTriggerEffect untilNextTurn(
            CardPredicate spellFilter, List<CardEffect> resolvedEffects,
            boolean sourceMustRemainOnBattlefield, TargetFilter targetFilter) {
        return new RegisterDelayedControllerSpellCastTriggerEffect(
                spellFilter, null, resolvedEffects, false, sourceMustRemainOnBattlefield,
                targetFilter, true, false);
    }

    /** Registers a source-independent one-shot boon that remains until the next matching spell is cast. */
    public static RegisterDelayedControllerSpellCastTriggerEffect oneShotUntilConsumed(
            CardPredicate spellFilter, List<CardEffect> resolvedEffects) {
        return new RegisterDelayedControllerSpellCastTriggerEffect(
                spellFilter, null, resolvedEffects, true, false, null, false, true);
    }
}
