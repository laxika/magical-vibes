package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * When resolved, registers a delayed triggered ability for the rest of the turn: "Until end of turn,
 * whenever you cast a [filter] spell, [resolvedEffects]." Registered by Mountain Titan.
 *
 * <p>The trigger belongs to the resolving controller and to the source permanent, so it stops firing
 * if that permanent leaves the battlefield. Activating the granting ability several times in a turn
 * registers several independent triggers, each of which fires on every matching spell.
 *
 * @param spellFilter     which cast spells fire the trigger; {@code null} = any spell
 * @param resolvedEffects effects put on the stack when it fires
 * @param sourceMustRemainOnBattlefield whether the source must still be on the battlefield
 */
public record RegisterDelayedControllerSpellCastTriggerEffect(CardPredicate spellFilter,
                                                              List<CardEffect> resolvedEffects,
                                                              boolean sourceMustRemainOnBattlefield)
        implements CardEffect {

    public RegisterDelayedControllerSpellCastTriggerEffect(CardPredicate spellFilter,
                                                            List<CardEffect> resolvedEffects) {
        this(spellFilter, resolvedEffects, true);
    }
}
