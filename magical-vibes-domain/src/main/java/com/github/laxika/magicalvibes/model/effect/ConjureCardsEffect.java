package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Objects;

/**
 * The conjure step (digital only): new cards from outside the game, owned by the effect's controller, go into one of
 * their zones ({@link ConjureDestination}), and the step records {@code StepKind.CONJURE} with each card in the zone it
 * is in afterwards, so later steps read them: "you may cast that card" is
 * {@code AllowPlayCardsFromExileEffect(Subjects.cardsThisWay(StepKind.CONJURE))}, "the duplicate perpetually gets
 * +1/+1" is {@code PerpetuallyBoostCardEffect.toCards(cardsThisWay(CONJURE), 1, 1)}, "then shuffle those duplicates
 * into your library" puts them there and shuffles.
 *
 * <p>The cards are one of {@link Cards}: a random one of listed printings each time ("a random card from [this]'s
 * spellbook"), or a duplicate of each object a subject names, which copies the copiable values of that object as it
 * is or last was (CR 707.2, 608.2h; a face-down permanent's are a nameless 2/2, CR 708.2a). A token has no card and
 * no duplicate. The log names a card only where the zone it goes to is public or the card was public already
 * (CR 400.2): a duplicate of a card in a graveyard, in exile or on the battlefield; a random pick going into a library
 * is "a card", and the library position is not logged.
 *
 * @param cards       what is conjured
 * @param count       how many cards: for printings, the number of picks; for duplicates, the number of duplicates of
 *                    each original ("conjure X duplicates of it")
 * @param destination where they go
 */
public record ConjureCardsEffect(Cards cards, DynamicAmount count, ConjureDestination destination)
        implements CardEffect {

    public ConjureCardsEffect {
        Objects.requireNonNull(cards, "cards");
        Objects.requireNonNull(count, "count");
        Objects.requireNonNull(destination, "destination");
    }

    /** "Conjure a random card from [this]'s spellbook into [destination]." */
    public static ConjureCardsEffect atRandomFrom(List<DraftFromSpellbookEffect.SpellbookCard> printings,
                                                  ConjureDestination destination) {
        return new ConjureCardsEffect(new AtRandomFrom(printings), new Fixed(1), destination);
    }

    /** "Conjure a duplicate of [it] into [destination]": the permanents {@code of} names, not targeted. */
    public static ConjureCardsEffect duplicateOf(PermanentSubject of, ConjureDestination destination) {
        return new ConjureCardsEffect(new DuplicatesOf(of, null), new Fixed(1), destination);
    }

    /** "Conjure a duplicate of target [filter] permanent into [destination]." */
    public static ConjureCardsEffect duplicateOfTarget(PermanentPredicate filter, ConjureDestination destination) {
        return new ConjureCardsEffect(new DuplicatesOf(Subjects.target(), filter), new Fixed(1), destination);
    }

    /** "Conjure a duplicate of [that card] into [destination]": the cards {@code of} names in a public zone. */
    public static ConjureCardsEffect duplicateOf(CardSubject of, ConjureDestination destination) {
        return new ConjureCardsEffect(new DuplicatesOfCards(of), new Fixed(1), destination);
    }

    /** This step conjuring {@code count} cards, or {@code count} duplicates of each original. */
    public ConjureCardsEffect times(DynamicAmount count) {
        return new ConjureCardsEffect(cards, count, destination);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (cards) {
            case DuplicatesOf of when of.of().declaresTarget() -> of.targetFilter() == null
                    ? TargetSpec.benign(TargetPredicates.permanent())
                    : TargetSpec.benign(TargetPredicates.permanent(), of.targetFilter());
            case DuplicatesOfCards of -> of.of().targetSpec();
            default -> TargetSpec.NONE;
        };
    }

    /** What a {@link ConjureCardsEffect} conjures. */
    public sealed interface Cards permits AtRandomFrom, DuplicatesOf, DuplicatesOfCards {
    }

    /**
     * Each card a random one of {@code printings}, among those implemented.
     *
     * @param printings the printings picked from
     */
    public record AtRandomFrom(List<DraftFromSpellbookEffect.SpellbookCard> printings) implements Cards {

        public AtRandomFrom {
            printings = List.copyOf(printings);
            if (printings.isEmpty()) {
                throw new IllegalArgumentException("A random conjure picks from at least one printing");
            }
        }
    }

    /**
     * A duplicate of each permanent {@code of} names, read as {@code SubjectResolutionService.cardsToDuplicate} reads
     * it.
     *
     * @param of           the originals
     * @param targetFilter what a target original must be, for a targeting subject; {@code null} for any permanent
     */
    public record DuplicatesOf(PermanentSubject of, PermanentPredicate targetFilter) implements Cards {

        public DuplicatesOf {
            Objects.requireNonNull(of, "of");
            if (targetFilter != null && !of.declaresTarget()) {
                throw new IllegalArgumentException("Only a target original has a target filter");
            }
        }
    }

    /**
     * A duplicate of each card {@code of} names, as it is in its zone.
     *
     * @param of the originals
     */
    public record DuplicatesOfCards(CardSubject of) implements Cards {

        public DuplicatesOfCards {
            Objects.requireNonNull(of, "of");
        }
    }
}
