package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Exile up to as many cards from the damaged player's graveyard as combat damage was dealt.
 * Creature cards exiled this way produce +1/+1 counters on the damage-dealing permanent, while
 * noncreature cards produce life gain for the ability's controller.
 */
public record ExileUpToCombatDamageCardsFromGraveyardEffect(DynamicAmount maxTargets)
        implements GraveyardCardChoosingEffect, CombatDamageAmountAwareEffect {

    public ExileUpToCombatDamageCardsFromGraveyardEffect() {
        this(new EventValue());
    }

    @Override
    public int graveyardChoiceMaxTargets() {
        return maxTargets instanceof Fixed fixed ? Math.max(0, fixed.value()) : 0;
    }

    @Override
    public DynamicAmount combatDamageAmount() {
        return maxTargets;
    }

    @Override
    public CardEffect snapshotCombatDamage(int damageDealt) {
        return new ExileUpToCombatDamageCardsFromGraveyardEffect(new Fixed(Math.max(0, damageDealt)));
    }
}
