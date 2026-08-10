package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Increases the casting cost of matching spells by the evaluated amount of generic mana.
 * Applied as a static effect from a permanent on the battlefield.
 *
 * <p>The {@code predicate} determines which spells are taxed; the {@code scope} determines whose
 * spells are taxed. It is the tax-side mirror of {@link ReduceCastCostForMatchingSpellsEffect}
 * and shares its {@link CostModificationScope}.
 *
 * <p>Examples:
 * <ul>
 *   <li>Thalia, Guardian of Thraben: {@code (CardNotPredicate(CardTypePredicate(CREATURE)), 1, ALL)}</li>
 *   <li>Derelor: {@code (CardColorPredicate(BLACK), 1, SELF)} — "Black spells you cast cost {B} more",
 *       the colored pip modeled as +1 generic</li>
 *   <li>Aura of Silence: {@code (artifactOrEnchantment, 2, OPPONENT)}</li>
 * </ul>
 */
public record IncreaseSpellCostEffect(CardPredicate predicate,
                                      DynamicAmount amount,
                                      CostModificationScope scope) implements CardEffect {

    /** Convenience for the common flat tax ("matching spells cost {N} more to cast"). */
    public IncreaseSpellCostEffect(CardPredicate predicate, int amount, CostModificationScope scope) {
        this(predicate, new Fixed(amount), scope);
    }
}
