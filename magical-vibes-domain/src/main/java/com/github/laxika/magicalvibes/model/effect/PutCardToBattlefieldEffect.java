package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Lets the controller put a card from their hand onto the battlefield.
 * <p>
 * The {@code predicate} filters which cards in hand are valid choices, and
 * the {@code label} is used in the prompt shown to the player (e.g. "creature", "land",
 * "historic permanent").
 * <p>
 * Typically wrapped in a {@link MayEffect} for "you may put" wording.
 *
 * @param predicate               filter for eligible cards in hand (e.g. {@code CardTypePredicate(CREATURE)},
 *                                {@code CardAllOfPredicate(CardIsHistoricPredicate, CardIsPermanentPredicate)})
 * @param label                   human-readable description of the card type for prompts (e.g. "creature", "historic permanent")
 * @param enterTapped             if {@code true}, the chosen card enters the battlefield tapped (e.g. Embrace the Paradox)
 * @param maxManaValueBoundedByX  if {@code true}, only cards whose mana value is at most the spell's X value
 *                                are eligible (e.g. Mind into Matter's "mana value X or less")
 * @param grantHaste              if {@code true}, the chosen card gains haste until end of turn (e.g. Incandescent Soulstoke)
 * @param sacrificeAtEndStep      if {@code true}, the chosen card is sacrificed at the beginning of the next end step
 *                                (e.g. Incandescent Soulstoke)
 */
public record PutCardToBattlefieldEffect(CardPredicate predicate, String label,
                                         boolean enterTapped, boolean maxManaValueBoundedByX,
                                         boolean grantHaste, boolean sacrificeAtEndStep) implements CardEffect {

    public PutCardToBattlefieldEffect(CardPredicate predicate, String label) {
        this(predicate, label, false, false, false, false);
    }

    public PutCardToBattlefieldEffect(CardPredicate predicate, String label, boolean enterTapped) {
        this(predicate, label, enterTapped, false, false, false);
    }

    public PutCardToBattlefieldEffect(CardPredicate predicate, String label, boolean enterTapped, boolean maxManaValueBoundedByX) {
        this(predicate, label, enterTapped, maxManaValueBoundedByX, false, false);
    }
}
