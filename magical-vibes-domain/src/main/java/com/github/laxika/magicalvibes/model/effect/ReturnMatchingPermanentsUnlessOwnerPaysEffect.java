package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Punisher mass bounce: for each permanent matching {@code filter} across all battlefields, return
 * it to its owner's hand unless that permanent's OWNER pays {@code manaCost} (Cut the Tethers, every
 * Spirit for {@code "{3}"}).
 *
 * <p>The payer is the owner, not the controller — "return it to its owner's hand unless that player
 * pays {3}" names the owner and then refers back to them. The two coincide until a permanent changes
 * hands; after that the player who may pay is the one whose hand would receive it, so a stolen Spirit
 * is kept or lost by the player it belongs to rather than the thief.
 *
 * <p>Each matching permanent is an independent decision made by its own owner, offered one at a time
 * through the may-ability system in APNAP order; declining — or accepting without the mana — bounces
 * that permanent and moves on to the next.
 *
 * @param filter which permanents are put to the choice (never null)
 * @param manaCost the mana an owner may pay to keep one matching permanent
 */
public record ReturnMatchingPermanentsUnlessOwnerPaysEffect(
        PermanentPredicate filter,
        String manaCost
) implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
