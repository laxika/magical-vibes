package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Objects;

/**
 * The conjure step (digital only): new cards from outside the game go into one of their owner's zones
 * ({@link ConjureDestination}), and the step records {@code StepKind.CONJURE} with each card in the zone it is in
 * afterwards, so later steps read them: "you may cast that card" is
 * {@code AllowPlayCardsFromExileEffect(Subjects.cardsThisWay(StepKind.CONJURE))}, "the duplicate perpetually gets
 * +1/+1" is {@code PerpetuallyBoostCardEffect.toCards(cardsThisWay(CONJURE), 1, 1)}, "then shuffle those duplicates
 * into your library" puts them there and shuffles.
 *
 * <p>The cards are one of {@link Cards}: a random one of listed printings each time ("a random card from [this]'s
 * spellbook"; "a card named X" is one printing), each of listed printings ("a card named A and a card named B",
 * "each card from [this]'s spellbook"), or a duplicate of each object a subject names, which copies the
 * copiable values of that object as it is or last was (CR 707.2, 608.2h; a face-down permanent's are a nameless 2/2,
 * CR 708.2a). A token has no card and no duplicate.
 *
 * <p>Who gets them: by default the controller conjures into their own zone and owns the cards. {@link #into} names
 * other players whose zones get them, each owning their cards, while the controller conjures ("into each player's
 * library"); {@link #conjuredBy} names the players who conjure, each into their own zone ("that player conjures a card
 * named Juggernaut into their hand"). Each conjuring player's cards are one conjure event (CR 603.2c). Cards conjured
 * onto the battlefield ({@link ConjureDestination#ontoBattlefield()}) enter as one batch under their owner's control
 * (CR 603.6a), tapped when the destination says so or when a later step makes them attack tapped; the step then records
 * the permanents, also as created this resolution ({@code Subjects.createdThisResolution()}).
 *
 * <p>The log names a card only where the zone it goes to is public, the card was public already, or the text names
 * it (CR 400.2): a card going onto the battlefield, a duplicate of a card in a graveyard, in exile, on the battlefield
 * or on the stack, or a card of a single printing; a random pick or a duplicate of a hidden card going into a hand or library is "a card"
 * ({@code GameLog.conjuresIntoHand} for a hand), and no library position is logged.
 *
 * @param cards                     what is conjured
 * @param count                     how many cards: for printings, the number of picks; for each of several printings,
 *                                  how many times each one's number; for duplicates, the number of duplicates of each
 *                                  original ("conjure X duplicates of it")
 * @param destination               where they go
 * @param owners                    whose zone gets the cards, each owning those put into theirs
 * @param ownersConjure             whether {@code owners} conjure (each into their own zone) rather than the controller
 * @param abilities                 abilities the new cards perpetually have from the start ("they perpetually gain
 *                                  storm"), part of each card as it is made, so they follow it to any zone
 * @param repeatWhileHandSmallerThan "then if you have fewer cards in hand than N, repeat this process": the conjure is
 *                                  done again while each owner's hand is smaller than this amount, evaluated after each
 *                                  time; {@code null} for once
 */
public record ConjureCardsEffect(Cards cards, DynamicAmount count, ConjureDestination destination,
                                 PlayerSubject owners, boolean ownersConjure, List<Ability> abilities,
                                 DynamicAmount repeatWhileHandSmallerThan)
        implements CardEffect {

    public ConjureCardsEffect {
        Objects.requireNonNull(cards, "cards");
        Objects.requireNonNull(count, "count");
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(owners, "owners");
        abilities = List.copyOf(abilities);
        if (owners.declaresTarget()) {
            throw new IllegalArgumentException("Who gets conjured cards is no target of this step: " + owners);
        }
        if (repeatWhileHandSmallerThan != null && destination.zone() != Zone.HAND) {
            throw new IllegalArgumentException("Only a conjure into a hand repeats while the hand is small");
        }
    }

    public ConjureCardsEffect(Cards cards, DynamicAmount count, ConjureDestination destination) {
        this(cards, count, destination, Subjects.you(), false, List.of(), null);
    }

    /** "Conjure a card named [it] into [destination]": the one printing the text names. */
    public static ConjureCardsEffect named(String setCode, String collectorNumber, ConjureDestination destination) {
        return atRandomFrom(List.of(new DraftFromSpellbookEffect.SpellbookCard(setCode, collectorNumber)),
                destination);
    }

    /** "Conjure a card named A and a card named B [onto the battlefield]": one card of each printing. */
    public static ConjureCardsEffect eachOf(List<DraftFromSpellbookEffect.SpellbookCard> printings,
                                            ConjureDestination destination) {
        return of(printings.stream().map(EachOf.Printing::once).toList(), destination);
    }

    /** "Conjure a card named A and X cards named B [onto the battlefield]": each printing that many times. */
    public static ConjureCardsEffect of(List<EachOf.Printing> printings, ConjureDestination destination) {
        return new ConjureCardsEffect(new EachOf(printings), new Fixed(1), destination);
    }

    /** "Conjure a random card from [this]'s spellbook into [destination]." */
    public static ConjureCardsEffect atRandomFrom(List<DraftFromSpellbookEffect.SpellbookCard> printings,
                                                  ConjureDestination destination) {
        return new ConjureCardsEffect(new AtRandomFrom(printings), new Fixed(1), destination);
    }

    /**
     * "Conjure a duplicate of [it] into [destination]": the permanents {@code of} names, not targeted; for
     * {@code Subjects.targetSpell()} "a duplicate of target spell", which this step declares.
     */
    public static ConjureCardsEffect duplicateOf(PermanentSubject of, ConjureDestination destination) {
        return new ConjureCardsEffect(new DuplicatesOf(of, null), new Fixed(1), destination);
    }

    /** "Conjure a duplicate of target [filter] permanent into [destination]." */
    public static ConjureCardsEffect duplicateOfTarget(PermanentPredicate filter, ConjureDestination destination) {
        return new ConjureCardsEffect(new DuplicatesOf(Subjects.target(), filter), new Fixed(1), destination);
    }

    /**
     * "Conjure a duplicate of [that card] into [destination]": the cards {@code of} names in a public zone; for
     * {@code Subjects.triggeringCardsInHand()} the cards an event put into a hand, as last known (CR 608.2h).
     */
    public static ConjureCardsEffect duplicateOf(CardSubject of, ConjureDestination destination) {
        return new ConjureCardsEffect(new DuplicatesOfCards(of), new Fixed(1), destination);
    }

    /** This step conjuring {@code count} cards, or {@code count} duplicates of each original. */
    public ConjureCardsEffect times(DynamicAmount count) {
        return new ConjureCardsEffect(cards, count, destination, owners, ownersConjure, abilities,
                repeatWhileHandSmallerThan);
    }

    /** {@link #times(DynamicAmount)} with a fixed number. */
    public ConjureCardsEffect times(int count) {
        return times(new Fixed(count));
    }

    /** "Into each player's [zone]", "into target player's hand": the controller conjures into those players' zones. */
    public ConjureCardsEffect into(PlayerSubject players) {
        return new ConjureCardsEffect(cards, count, destination, players, false, abilities,
                repeatWhileHandSmallerThan);
    }

    /** "That player conjures ... into their [zone]": each of {@code players} conjures into their own zone. */
    public ConjureCardsEffect conjuredBy(PlayerSubject players) {
        return new ConjureCardsEffect(cards, count, destination, players, true, abilities,
                repeatWhileHandSmallerThan);
    }

    /** "They perpetually gain [abilities]": each new card has them from the start. */
    public ConjureCardsEffect withAbilities(List<? extends Ability> perpetual) {
        return new ConjureCardsEffect(cards, count, destination, owners, ownersConjure, List.copyOf(perpetual),
                repeatWhileHandSmallerThan);
    }

    /** "Then if you have fewer cards in hand than {@code amount}, repeat this process." */
    public ConjureCardsEffect repeatingWhileHandSmallerThan(DynamicAmount amount) {
        return new ConjureCardsEffect(cards, count, destination, owners, ownersConjure, abilities,
                Objects.requireNonNull(amount, "amount"));
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (cards) {
            // "Conjure a duplicate of target spell": the spell target is this step's own (CR 115.1).
            case DuplicatesOf of when of.of() instanceof PermanentSubject.TargetSpell ->
                    TargetSpec.benign(TargetPredicates.spellOnStack());
            case DuplicatesOf of when of.of().declaresTarget() -> of.targetFilter() == null
                    ? TargetSpec.benign(TargetPredicates.permanent())
                    : TargetSpec.benign(TargetPredicates.permanent(), of.targetFilter());
            case DuplicatesOfCards of -> of.of().targetSpec();
            default -> TargetSpec.NONE;
        };
    }

    /** What a {@link ConjureCardsEffect} conjures. */
    public sealed interface Cards permits AtRandomFrom, EachOf, DuplicatesOf, DuplicatesOfCards {
    }

    /**
     * Each of {@code printings}, each its own number of cards, among those implemented; a printing named twice is
     * conjured for each mention.
     *
     * @param printings the printings and how many cards of each
     */
    public record EachOf(List<Printing> printings) implements Cards {

        public EachOf {
            printings = List.copyOf(printings);
            if (printings.isEmpty()) {
                throw new IllegalArgumentException("Each of no printings conjures nothing");
            }
        }

        /**
         * One printing of an {@link EachOf} and how many cards of it.
         *
         * @param printing the printing
         * @param count    how many cards of it
         */
        public record Printing(DraftFromSpellbookEffect.SpellbookCard printing, DynamicAmount count) {

            public Printing {
                Objects.requireNonNull(printing, "printing");
                Objects.requireNonNull(count, "count");
            }

            /** One card of {@code printing}. */
            public static Printing once(DraftFromSpellbookEffect.SpellbookCard printing) {
                return new Printing(printing, new Fixed(1));
            }

            /** {@code count} cards of the printing {@code setCode} {@code collectorNumber}. */
            public static Printing of(String setCode, String collectorNumber, DynamicAmount count) {
                return new Printing(new DraftFromSpellbookEffect.SpellbookCard(setCode, collectorNumber), count);
            }
        }
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
