package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reduces cast cost of matching spells by the given amount of generic mana.
 * Applied as a static effect from a permanent on the battlefield.
 *
 * <p>The {@code predicate} determines which spells are affected (e.g. historic, creature, artifact).
 * The {@code scope} determines whose spells are affected (SELF = controller, OPPONENT = opponents).
 *
 * <p>Examples:
 * <ul>
 *   <li>Jhoira's Familiar: {@code new ReduceCastCostForMatchingSpellsEffect(new CardIsHistoricPredicate(), 1, SELF)}</li>
 *   <li>Goblin Warchief: {@code new ReduceCastCostForMatchingSpellsEffect(new CardSubtypePredicate(GOBLIN), 1, SELF)}</li>
 *   <li>Danitha Capashen, Paragon: {@code new ReduceCastCostForMatchingSpellsEffect(new CardAnyOfPredicate(List.of(new CardSubtypePredicate(AURA), new CardSubtypePredicate(EQUIPMENT))), 1, SELF)}</li>
 * </ul>
 */
public record ReduceCastCostForMatchingSpellsEffect(
        CardPredicate predicate,
        int amount,
        CostModificationScope scope
) implements CardEffect {
}
