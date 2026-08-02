package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Punisher mass bounce: for each permanent matching {@code filter} across all battlefields, return
 * it to its owner's hand unless that permanent's controller pays {@code manaCost} (Cut the Tethers,
 * every Spirit for {@code "{3}"}).
 *
 * <p>Each matching permanent is an independent decision made by its own controller, offered one at a
 * time through the may-ability system in APNAP order; declining — or accepting without the mana —
 * bounces that permanent and moves on to the next.
 *
 * @param filter which permanents are put to the choice (never null)
 * @param manaCost the mana a controller may pay to keep one matching permanent
 */
public record ReturnMatchingPermanentsUnlessControllerPaysEffect(
        PermanentPredicate filter,
        String manaCost
) implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
