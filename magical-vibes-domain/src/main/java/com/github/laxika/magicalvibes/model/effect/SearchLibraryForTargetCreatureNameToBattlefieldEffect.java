package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

/**
 * Searches the controller's library for a card with the same name as the target nontoken creature
 * and puts it onto the battlefield, then shuffles.
 *
 * @param permanentCardOnly whether to restrict the search to permanent cards
 * @param creatureCardOnly whether to restrict the search to creature cards
 * @param destination where to put the chosen card
 * @param targetRestriction an additional restriction on the target creature
 * @param allowTokenTargets whether the target may be a token ("target creature" rather than
 *                          "target nontoken creature"); a token's name is still read on resolution
 */
public record SearchLibraryForTargetCreatureNameToBattlefieldEffect(
        boolean permanentCardOnly,
        boolean creatureCardOnly,
        LibrarySearchDestination destination,
        PermanentPredicate targetRestriction,
        boolean allowTokenTargets
) implements CardEffect {

    public SearchLibraryForTargetCreatureNameToBattlefieldEffect(
            boolean permanentCardOnly, boolean creatureCardOnly, LibrarySearchDestination destination,
            PermanentPredicate targetRestriction) {
        this(permanentCardOnly, creatureCardOnly, destination, targetRestriction, false);
    }

    public SearchLibraryForTargetCreatureNameToBattlefieldEffect() {
        this(false, false, LibrarySearchDestination.BATTLEFIELD, null);
    }

    public SearchLibraryForTargetCreatureNameToBattlefieldEffect(boolean permanentCardOnly) {
        this(permanentCardOnly, false, LibrarySearchDestination.BATTLEFIELD, null);
    }

    @Override
    public TargetSpec targetSpec() {
        PermanentPredicate restriction;
        if (allowTokenTargets) {
            restriction = targetRestriction == null ? new PermanentTruePredicate() : targetRestriction;
        } else {
            PermanentPredicate nontokenCreature = new PermanentNotPredicate(new PermanentIsTokenPredicate());
            restriction = targetRestriction == null
                    ? nontokenCreature
                    : new PermanentAllOfPredicate(List.of(nontokenCreature, targetRestriction));
        }
        return TargetSpec.benign(TargetPredicates.creature(), restriction);
    }
}
