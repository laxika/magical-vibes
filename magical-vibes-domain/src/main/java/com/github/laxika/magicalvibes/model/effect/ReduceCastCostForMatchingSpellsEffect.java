package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reduces cast cost of matching spells by the evaluated amount of generic mana.
 * Applied as a static effect from a permanent on the battlefield.
 *
 * <p>The {@code predicate} determines which spells are affected (e.g. historic, creature, artifact).
 * The {@code scope} determines whose spells are affected (SELF = controller, OPPONENT = opponents).
 * The {@code amount} is evaluated against the source permanent, so source-relative amounts
 * ({@code CountersOnSource}) express "for each counter on this creature" wordings.
 *
 * <p>Examples:
 * <ul>
 *   <li>Jhoira's Familiar: {@code new ReduceCastCostForMatchingSpellsEffect(new CardIsHistoricPredicate(), 1, SELF)}</li>
 *   <li>Goblin Warchief: {@code new ReduceCastCostForMatchingSpellsEffect(new CardSubtypePredicate(GOBLIN), 1, SELF)}</li>
 *   <li>Danitha Capashen, Paragon: {@code new ReduceCastCostForMatchingSpellsEffect(new CardAnyOfPredicate(List.of(new CardSubtypePredicate(AURA), new CardSubtypePredicate(EQUIPMENT))), 1, SELF)}</li>
 *   <li>Herald of War: {@code new ReduceCastCostForMatchingSpellsEffect(anglesOrHumans, new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE), SELF)}</li>
 * </ul>
 */
public record ReduceCastCostForMatchingSpellsEffect(
        CardPredicate predicate,
        DynamicAmount amount,
        CostModificationScope scope
) implements CardEffect {

    /** Convenience for the common flat reduction ("matching spells cost {N} less to cast"). */
    public ReduceCastCostForMatchingSpellsEffect(CardPredicate predicate, int amount, CostModificationScope scope) {
        this(predicate, new Fixed(amount), scope);
    }
}
