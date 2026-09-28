package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Registers a delayed trigger watching the player chosen as the enclosing ability's target.
 * This is the targeted counterpart to {@link RegisterDelayedControllerSpellCastTriggerEffect}.
 */
public record RegisterDelayedTargetPlayerSpellCastTriggerEffect(
        CardPredicate spellFilter,
        List<CardEffect> resolvedEffects,
        boolean oneShot,
        boolean sourceMustRemainOnBattlefield
) implements CardEffect {
}
