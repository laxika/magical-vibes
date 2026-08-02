package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals the top X cards of the controller's library (X from the stack entry's X value), lets the
 * controller put up to {@code maxSelections} revealed cards matching {@code eligiblePredicate} whose
 * mana value is &le; X onto the battlefield, then shuffles the rest into their library.
 *
 * <p>Sibling of {@link LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect} and
 * {@link GenesisWaveEffect}: same reveal-then-choose flow, but the not-chosen cards are shuffled
 * back into the library instead of being binned or bottomed, and the pick is capped.
 *
 * <p>Used by Genesis Hydra ({@code CardAllOfPredicate(CardIsPermanentPredicate,
 * CardNotPredicate(CardTypePredicate(LAND)))}, {@code maxSelections = 1}) from its
 * {@code ON_SELF_CAST} slot.
 *
 * @param eligiblePredicate which revealed cards may be chosen (mana value &le; X is checked on top)
 * @param maxSelections how many revealed cards may be put onto the battlefield
 */
public record RevealTopXCardsMayPutPermanentToBattlefieldShuffleRestEffect(
        CardPredicate eligiblePredicate,
        int maxSelections
) implements CardEffect {
}
