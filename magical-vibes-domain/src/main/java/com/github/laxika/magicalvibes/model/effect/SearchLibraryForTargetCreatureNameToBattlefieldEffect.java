package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

/**
 * Searches the controller's library for a card with the same name as the target nontoken creature
 * and puts it onto the battlefield, then shuffles.
 *
 * @param permanentCardOnly whether to restrict the search to permanent cards
 * @param creatureCardOnly whether to restrict the search to creature cards
 * @param destination where to put the chosen card
 * @param targetRestriction an additional restriction on the target creature
 */
public record SearchLibraryForTargetCreatureNameToBattlefieldEffect(
        boolean permanentCardOnly,
        boolean creatureCardOnly,
        LibrarySearchDestination destination,
        PermanentPredicate targetRestriction
) implements CardEffect {

    public SearchLibraryForTargetCreatureNameToBattlefieldEffect() {
        this(false, false, LibrarySearchDestination.BATTLEFIELD, null);
    }

    public SearchLibraryForTargetCreatureNameToBattlefieldEffect(boolean permanentCardOnly) {
        this(permanentCardOnly, false, LibrarySearchDestination.BATTLEFIELD, null);
    }

    @Override
    public TargetSpec targetSpec() {
        PermanentPredicate nontokenCreature = new PermanentNotPredicate(new PermanentIsTokenPredicate());
        PermanentPredicate restriction = targetRestriction == null
                ? nontokenCreature
                : new PermanentAllOfPredicate(List.of(nontokenCreature, targetRestriction));
        return TargetSpec.benign(TargetPredicates.creature(), restriction);
    }
}
