package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;
import java.util.Objects;

/**
 * "Choose an instant or sorcery card exiled this way", "a creature card from among them", "one of them": the
 * controller chooses among the cards {@code cards} names as the effect resolves (CR 608.2d). No card moves. The step
 * resolves itself again with each answer and records what was chosen as {@code StepKind.CHOOSE}, so what is done with
 * "it" or "those cards" is a later step over {@code Subjects.cardsThisWay(StepKind.CHOOSE)}, for all of them at once;
 * with no card named it records nothing. "You may ... one of them" is {@link #upTo} one, or this inside a
 * {@code MayEffect}, with what is done with the card.
 *
 * <p>"Return up to X cards from among cards exiled with this", "any number of creature cards milled this way" is
 * {@link #upTo}, "return two cards exiled with this" {@link #exactly}; "return a creature card and a land card" is
 * {@link #oneOfEach}, "for each permanent type, return up to one card of that type" {@link #upToOneOfEach}: one
 * choice after another, a card chosen once no longer offered where one has to be chosen (choosing it again where none
 * has to be is choosing none), or at all where the cards leave those chosen among ({@link #offeringEachOnce}: "for each
 * card type, you may put a card of that type from among the revealed cards into your hand").
 *
 * <p>"Its controller may put a creature card from their hand ..." names who chooses ({@link #by}); "choose a creature
 * card at random", "a random instant card" is nobody's choice ({@link #atRandom}): nothing is asked or shown.
 *
 * <p>The player choosing sees the cards offered, so {@code cards} names only cards that player may look at. Of a hand
 * ({@code Subjects.cardsInHands}) only their own cards are offered, the prompt is theirs alone and no line names the
 * card (CR 402.3); a library's are only chosen at random.
 *
 * @param cards       among which cards
 * @param label       what the prompt calls them ("an instant or sorcery card exiled this way")
 * @param alwaysAsked whether the choice is announced when nothing is left to choose, as a card that says "choose"
 *                    has it; otherwise a lone card is taken unasked ("put a creature card from among them onto the
 *                    battlefield")
 * @param count       how many are chosen, as many as there are if fewer
 * @param upTo        whether fewer, or none, may be chosen
 * @param each        the qualities one card is chosen for each of, in this order; empty for none
 * @param chooser     who chooses; {@code null} for nobody, where the cards are chosen at random
 * @param offeredOnce whether a card chosen for one quality isn't offered for the next also where none has to be
 */
public record ChooseCardEffect(CardSubject cards, String label, boolean alwaysAsked, DynamicAmount count,
                               boolean upTo, List<Each> each, PlayerSubject chooser, boolean offeredOnce)
        implements CardEffect {

    /** One of the qualities a card is chosen for: "a creature card", "a land card". */
    public record Each(CardPredicate filter, String label) {

        public Each {
            Objects.requireNonNull(filter, "filter");
            Objects.requireNonNull(label, "label");
        }
    }

    public ChooseCardEffect {
        Objects.requireNonNull(cards, "cards");
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(count, "count");
        each = List.copyOf(each);
        if (cards instanceof CardSubject.TargetCard || cards instanceof CardSubject.TargetExiledCard) {
            throw new IllegalArgumentException("A resolution-time choice targets no card: " + cards);
        }
        if (chooser != null && chooser.declaresTarget()) {
            throw new IllegalArgumentException("Who chooses is no target of this step: " + chooser);
        }
        if ((cards instanceof CardSubject.InLibrary || cards instanceof CardSubject.TopmostInLibrary) && chooser != null) {
            throw new IllegalArgumentException("Nobody is shown a library to choose from (CR 400.2): " + cards);
        }
        if (chooser == null && !each.isEmpty()) {
            throw new IllegalArgumentException("One card for each quality is a player's choice");
        }
        if (offeredOnce && each.isEmpty()) {
            throw new IllegalArgumentException("Only a choice for each of several qualities offers a card again");
        }
    }

    /** A choice offering a card again for another quality where none has to be chosen. */
    public ChooseCardEffect(CardSubject cards, String label, boolean alwaysAsked, DynamicAmount count,
                            boolean upTo, List<Each> each, PlayerSubject chooser) {
        this(cards, label, alwaysAsked, count, upTo, each, chooser, false);
    }

    /** This choice, not offering a card again once it is chosen for one of the qualities. */
    public ChooseCardEffect offeringEachOnce() {
        return new ChooseCardEffect(cards, label, alwaysAsked, count, upTo, each, chooser, true);
    }

    /** The controller's choice. */
    public ChooseCardEffect(CardSubject cards, String label, boolean alwaysAsked, DynamicAmount count,
                            boolean upTo, List<Each> each) {
        this(cards, label, alwaysAsked, count, upTo, each, Subjects.you());
    }

    /** "Choose a creature card at random from target opponent's graveyard", "a random instant card". */
    public static ChooseCardEffect atRandom(CardSubject cards, String label) {
        return new ChooseCardEffect(cards, label, false, new Fixed(1), false, List.of(), null);
    }

    /** "Its controller may put ... from their hand": this choice made by the player {@code player} names. */
    public ChooseCardEffect by(PlayerSubject player) {
        return new ChooseCardEffect(cards, label, alwaysAsked, count, upTo, each, player, offeredOnce);
    }

    /** The player whose cards are chosen among, where that is a target. */
    @Override
    public TargetSpec targetSpec() {
        return cards.targetSpec();
    }

    /** "Choose [label]": announced also when only one card qualifies. */
    public static ChooseCardEffect announced(CardSubject cards, String label) {
        return new ChooseCardEffect(cards, label, true, new Fixed(1), false, List.of());
    }

    /** "[Label] from among them", "one of them": a lone card is the one. */
    public static ChooseCardEffect oneOf(CardSubject cards, String label) {
        return new ChooseCardEffect(cards, label, false, new Fixed(1), false, List.of());
    }

    /** "Up to [max] [label]": always asked while a card is named. */
    public static ChooseCardEffect upTo(CardSubject cards, DynamicAmount max, String label) {
        return new ChooseCardEffect(cards, label, true, max, true, List.of());
    }

    /** "Up to [max] [label]": {@link #upTo(CardSubject, DynamicAmount, String)} of a number. */
    public static ChooseCardEffect upTo(CardSubject cards, int max, String label) {
        return upTo(cards, new Fixed(max), label);
    }

    /** "[Count] [label]": when there are no more than that, those are the ones. */
    public static ChooseCardEffect exactly(CardSubject cards, int count, String label) {
        return new ChooseCardEffect(cards, label, false, new Fixed(count), false, List.of());
    }

    /** "A creature card and a land card": one card for each quality, a lone one taken unasked. */
    public static ChooseCardEffect oneOfEach(CardSubject cards, Each... each) {
        return new ChooseCardEffect(cards, "a card for each", false, new Fixed(1), false, List.of(each));
    }

    /** "For each permanent type, up to one card of that type": one optional choice for each quality. */
    public static ChooseCardEffect upToOneOfEach(CardSubject cards, Each... each) {
        return new ChooseCardEffect(cards, "a card for each", true, new Fixed(1), true, List.of(each));
    }
}
