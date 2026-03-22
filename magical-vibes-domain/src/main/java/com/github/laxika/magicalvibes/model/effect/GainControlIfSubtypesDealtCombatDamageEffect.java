package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * End-step triggered ability: gain control of target nonland permanent controlled by a player
 * who was dealt combat damage by {@code threshold} or more creatures of the given {@code subtype}
 * this turn.
 *
 * <p>The intervening-if condition (whether any opponent qualifies) and target restriction
 * (nonland permanents of qualifying opponents) are handled in {@code StepTriggerService}.
 * Once on the stack, the actual resolution delegates to {@link GainControlOfTargetPermanentEffect}.
 *
 * @param subtype   the creature subtype to check (e.g. Pirate)
 * @param threshold minimum number of distinct creatures of that subtype required
 */
public record GainControlIfSubtypesDealtCombatDamageEffect(CardSubtype subtype, int threshold) implements CardEffect {
}
