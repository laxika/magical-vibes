package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/**
 * Puts a target spell, nonland permanent, or graveyard card on top or bottom of its owner's
 * library, as chosen by that target's owner.
 */
public record PutTargetSpellOrPermanentOrGraveyardCardOnTopOrBottomOfLibraryEffect(
        Destination destination) implements CardEffect {

    public PutTargetSpellOrPermanentOrGraveyardCardOnTopOrBottomOfLibraryEffect() {
        this(Destination.CHOOSE);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.anyOf(
                TargetPredicates.permanents(new PermanentNotPredicate(new PermanentIsLandPredicate())),
                TargetPredicates.spellOnStack(),
                TargetPredicates.graveyardCard(GraveyardSearchScope.ALL_GRAVEYARDS)));
    }

    public enum Destination {
        CHOOSE,
        TOP,
        BOTTOM
    }
}
