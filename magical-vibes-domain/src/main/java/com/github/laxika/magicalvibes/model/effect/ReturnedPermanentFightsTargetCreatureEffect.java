package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Queues a reflexive ability for the permanent created by a preceding targeted graveyard return.
 * The reflexive ability has that returned permanent fight up to one target creature matching the
 * supplied predicate.
 *
 * <p>This effect is intentionally unbound: the preceding graveyard return keeps the graveyard card
 * target, while the follow-up target is chosen only after the return succeeds.</p>
 *
 * @param targetPredicate restriction for the creature that may be chosen for the fight
 */
public record ReturnedPermanentFightsTargetCreatureEffect(PermanentPredicate targetPredicate)
        implements CardEffect {
}
