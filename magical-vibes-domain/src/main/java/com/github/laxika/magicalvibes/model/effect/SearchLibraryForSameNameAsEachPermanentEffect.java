package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "For each [permanent], [you may] search [a player's] library for a [creature] card with the same name as that
 * permanent. Put those cards onto the battlefield [tapped] [under your control], then shuffle": one search of the
 * {@code libraryOwner}'s library by the effect's controller, with one pick for each permanent {@code permanents} names
 * (two permanents with one name give two picks, CR 608.2d); a permanent with no name, such as a face-down one, gives a
 * pick nothing can match (CR 201.2a). The names are read as the effect applies: a permanent's name as changed by an
 * effect (a Clone) counts. The found cards stay in the library until the last pick, then enter together under the
 * controller's control (CR 608.2e), and the library is shuffled. Doubling Chant ({@code controlledBy(you(), creature)},
 * creature cards only), Clarion Ultimatum (after {@code ChoosePermanentsEffect.exactly(5, …)}, {@code thisWay(CHOOSE)},
 * tapped), Dichotomancy ({@code controlledBy(targetPlayer(), tapped nonland)} and that player's library). Records
 * {@code StepKind.SEARCH}.
 *
 * @param permanents   for each of which a card with its name is searched for; a player target in the set (its
 *                     controller) is the effect's target
 * @param libraryOwner whose library is searched; never a target of its own
 * @param creatureOnly whether only creature cards may be found ("a creature card with the same name")
 * @param tapped       whether the found cards enter tapped
 * @param optional     "you may search" (the prompt asks); a search for a card with a stated quality may find nothing
 *                     anyway (CR 701.23b)
 */
public record SearchLibraryForSameNameAsEachPermanentEffect(PermanentSubject permanents, PlayerSubject libraryOwner,
                                                            boolean creatureOnly, boolean tapped, boolean optional)
        implements CardEffect {

    public SearchLibraryForSameNameAsEachPermanentEffect {
        Objects.requireNonNull(permanents, "permanents");
        Objects.requireNonNull(libraryOwner, "libraryOwner");
        if (libraryOwner.declaresTarget()) {
            throw new IllegalArgumentException("Whose library is searched is no target of its own: " + libraryOwner);
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return permanents.declaresPlayerTarget() ? TargetSpec.harmful(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
