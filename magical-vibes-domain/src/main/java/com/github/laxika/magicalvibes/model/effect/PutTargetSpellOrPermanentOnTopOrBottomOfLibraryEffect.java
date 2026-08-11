package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;

import java.util.Set;

/**
 * Asks the target's owner to put a red or green spell or permanent on top or bottom of its
 * owner's library.
 */
public record PutTargetSpellOrPermanentOnTopOrBottomOfLibraryEffect(Destination destination)
        implements CardEffect {

    private static final Set<CardColor> RED_OR_GREEN = Set.of(CardColor.RED, CardColor.GREEN);

    public PutTargetSpellOrPermanentOnTopOrBottomOfLibraryEffect() {
        this(Destination.CHOOSE);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.anyOf(
                TargetPredicates.permanents(new PermanentColorInPredicate(RED_OR_GREEN)),
                TargetPredicates.spells(new StackEntryColorInPredicate(RED_OR_GREEN))));
    }

    public enum Destination {
        CHOOSE,
        TOP,
        BOTTOM
    }
}
