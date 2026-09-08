package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

import java.util.Set;

/**
 * Asks the target's owner to put a spell or permanent matching the supplied predicates on top or
 * bottom of its owner's library.
 */
public record PutTargetSpellOrPermanentOnTopOrBottomOfLibraryEffect(
        PermanentPredicate permanentPredicate,
        StackEntryPredicate spellPredicate,
        Destination destination)
        implements CardEffect {

    private static final Set<CardColor> RED_OR_GREEN = Set.of(CardColor.RED, CardColor.GREEN);

    public PutTargetSpellOrPermanentOnTopOrBottomOfLibraryEffect() {
        this(new PermanentColorInPredicate(RED_OR_GREEN), new StackEntryColorInPredicate(RED_OR_GREEN),
                Destination.CHOOSE);
    }

    public PutTargetSpellOrPermanentOnTopOrBottomOfLibraryEffect(Destination destination) {
        this(new PermanentColorInPredicate(RED_OR_GREEN), new StackEntryColorInPredicate(RED_OR_GREEN),
                destination);
    }

    public PutTargetSpellOrPermanentOnTopOrBottomOfLibraryEffect(
            PermanentPredicate permanentPredicate, StackEntryPredicate spellPredicate) {
        this(permanentPredicate, spellPredicate, Destination.CHOOSE);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.anyOf(
                TargetPredicates.permanents(permanentPredicate),
                TargetPredicates.spells(spellPredicate)));
    }

    public enum Destination {
        CHOOSE,
        TOP,
        BOTTOM
    }
}
