package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import java.util.Set;

/**
 * Spell effect: each opponent of the controller can't cast spells of the specified types during
 * that opponent's next turn.
 */
public record OpponentsCantCastSpellTypesNextTurnEffect(Set<CardType> restrictedTypes)
        implements CardEffect {
}
