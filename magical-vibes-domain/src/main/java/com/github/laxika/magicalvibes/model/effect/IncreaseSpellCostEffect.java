package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.Zone;

import java.util.Set;

/**
 * Increases the casting cost of matching spells by evaluated generic mana or by a fixed mana cost.
 * Applied as a static effect from a permanent on the battlefield.
 *
 * <p>The {@code predicate} determines which spells are taxed; the {@code scope} determines whose
 * spells are taxed. It is the tax-side mirror of {@link ReduceCastCostForMatchingSpellsEffect}
 * and shares its {@link CostModificationScope}.
 *
 * <p>Examples:
 * <ul>
 *   <li>Thalia, Guardian of Thraben: {@code (CardNotPredicate(CardTypePredicate(CREATURE)), 1, ALL)}</li>
 *   <li>Derelor: {@code (CardColorPredicate(BLACK), "{B}", SELF)}</li>
 *   <li>Aura of Silence: {@code (artifactOrEnchantment, 2, OPPONENT)}</li>
 * </ul>
 */
public record IncreaseSpellCostEffect(CardPredicate predicate,
                                      DynamicAmount amount,
                                      String manaCost,
                                      CostModificationScope scope,
                                      Set<Zone> sourceZones) implements CardEffect {

    public IncreaseSpellCostEffect(CardPredicate predicate, DynamicAmount amount,
                                   String manaCost, CostModificationScope scope) {
        this(predicate, amount, manaCost, scope, Set.of());
    }

    /** Convenience for the common flat tax ("matching spells cost {N} more to cast"). */
    public IncreaseSpellCostEffect(CardPredicate predicate, int amount, CostModificationScope scope) {
        this(predicate, new Fixed(amount), null, scope, Set.of());
    }

    public IncreaseSpellCostEffect(CardPredicate predicate, DynamicAmount amount, CostModificationScope scope) {
        this(predicate, amount, null, scope, Set.of());
    }

    /** Convenience for a fixed colored or hybrid increase such as "cost {B} more to cast". */
    public IncreaseSpellCostEffect(CardPredicate predicate, String manaCost, CostModificationScope scope) {
        this(predicate, null, manaCost, scope, Set.of());
    }

    public IncreaseSpellCostEffect(CardPredicate predicate, int amount,
                                   CostModificationScope scope, Set<Zone> sourceZones) {
        this(predicate, new Fixed(amount), null, scope, Set.copyOf(sourceZones));
    }
}
