package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Looks at the top X cards of the controller's library (X from the spell's X value),
 * lets the controller choose any number of eligible cards to put onto the battlefield,
 * and puts the rest into the graveyard.
 *
 * <p>Eligibility is determined by two predicates:
 * <ul>
 *   <li>{@code alwaysEligiblePredicate} — cards matching this are eligible regardless of mana value.
 *       May be {@code null} if no cards bypass the MV check.</li>
 *   <li>{@code mvCappedEligiblePredicate} — cards matching this are eligible only if their
 *       mana value is &le; X. May be {@code null} if no cards use this path.</li>
 * </ul>
 *
 * <p>A card is eligible if it matches either predicate (with the respective MV constraint).
 *
 * <p>Examples:
 * <ul>
 *   <li>Kamahl's Druidic Vow: {@code alwaysEligible = CardTypePredicate(LAND)},
 *       {@code mvCapped = CardAllOfPredicate(CardSupertypePredicate(LEGENDARY), CardIsPermanentPredicate())}</li>
 *   <li>Genesis Wave: {@code alwaysEligible = null},
 *       {@code mvCapped = CardIsPermanentPredicate()}</li>
 * </ul>
 *
 * @param alwaysEligiblePredicate cards matching this are eligible regardless of mana value (nullable)
 * @param mvCappedEligiblePredicate cards matching this are eligible only if MV &le; X (nullable)
 */
public record LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect(
        CardPredicate alwaysEligiblePredicate,
        CardPredicate mvCappedEligiblePredicate
) implements CardEffect {
}
