package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

/**
 * When resolved, registers a delayed triggered ability for the rest of the turn: "Until end of turn,
 * whenever you cast a [filter] spell, [resolvedEffects]." Registered by Mountain Titan.
 *
 * <p>The trigger belongs to the resolving controller and normally remains tied to the source
 * permanent, so it stops firing if that permanent leaves the battlefield. Set
 * {@code sourceMustRemainOnBattlefield} to false for delayed abilities that continue after their
 * source leaves. Activating the granting ability several times in a turn registers several
 * independent triggers, each of which fires on every matching spell.
 *
 * @param spellFilter                  which cast spells fire the trigger; {@code null} = any spell
 * @param stackEntryFilter             optional filter evaluated against the cast stack entry
 * @param resolvedEffects              effects put on the stack when it fires
 * @param sourceMustRemainOnBattlefield whether the source permanent must still be on the battlefield
 * @param targetFilter                 optional permanent/player target filter used when the delayed
 *                                     trigger goes on the stack
 */
public record RegisterDelayedControllerSpellCastTriggerEffect(CardPredicate spellFilter,
                                                               StackEntryPredicate stackEntryFilter,
                                                               List<CardEffect> resolvedEffects,
                                                               boolean oneShot,
                                                               boolean sourceMustRemainOnBattlefield,
                                                               TargetFilter targetFilter)
        implements CardEffect {

    public RegisterDelayedControllerSpellCastTriggerEffect(CardPredicate spellFilter,
                                                            List<CardEffect> resolvedEffects) {
        this(spellFilter, null, resolvedEffects, false, true, null);
    }

    public RegisterDelayedControllerSpellCastTriggerEffect(CardPredicate spellFilter,
                                                            List<CardEffect> resolvedEffects,
                                                            boolean sourceMustRemainOnBattlefield) {
        this(spellFilter, null, resolvedEffects, false, sourceMustRemainOnBattlefield, null);
    }

    public RegisterDelayedControllerSpellCastTriggerEffect(CardPredicate spellFilter,
                                                            List<CardEffect> resolvedEffects,
                                                            boolean sourceMustRemainOnBattlefield,
                                                            TargetFilter targetFilter) {
        this(spellFilter, null, resolvedEffects, false, sourceMustRemainOnBattlefield, targetFilter);
    }

    public RegisterDelayedControllerSpellCastTriggerEffect(CardPredicate spellFilter,
                                                            List<CardEffect> resolvedEffects,
                                                            boolean oneShot,
                                                            boolean sourceMustRemainOnBattlefield) {
        this(spellFilter, null, resolvedEffects, oneShot, sourceMustRemainOnBattlefield, null);
    }

    public static RegisterDelayedControllerSpellCastTriggerEffect withStackEntryFilter(
            StackEntryPredicate stackEntryFilter, List<CardEffect> resolvedEffects,
            boolean sourceMustRemainOnBattlefield) {
        return new RegisterDelayedControllerSpellCastTriggerEffect(
                null, stackEntryFilter, resolvedEffects, false, sourceMustRemainOnBattlefield, null);
    }
}
