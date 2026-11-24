package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

import java.util.Objects;

/**
 * Which cards outside the battlefield an effect acts on, the third subject type beside {@link PermanentSubject} and
 * {@link PlayerSubject}: "the exiled card", "each card put into a graveyard this way", "target creature card in your
 * graveyard". Built through {@link Subjects}; resolved only by the engine's {@code SubjectResolutionService}, which
 * names each card as it is now, in the zone it is expected to be in, and nothing for a card that has left that
 * zone (a card that moves is a new object, CR 400.7). Two variants name cards in a hidden zone, {@link InHand} and
 * {@link InLibrary} ({@link #inHiddenZone()}): they say nothing about who may see those cards.
 *
 * <p>{@link TargetCard} and {@link TargetExiledCard} declare a card target, and a variant of a target player's cards
 * that player ({@link #declaresTarget()}, {@link #targetSpec()}). Every variant reads a stack entry, so a
 * static ability can't use one.
 *
 * <p>The variants and the wording each stands for are listed in {@code agent-docs/SUBJECTS_REFERENCE.md}.
 */
public sealed interface CardSubject permits CardSubject.CardsThisWay, CardSubject.TargetCard,
        CardSubject.TriggeringCards, CardSubject.SourceCard, CardSubject.InYourGraveyard,
        CardSubject.InGraveyards, CardSubject.InExile, CardSubject.ExiledWithSource, CardSubject.Except,
        CardSubject.TargetExiledCard, CardSubject.TopOfGraveyard, CardSubject.InHand, CardSubject.InLibrary,
        CardSubject.InCommandZone, CardSubject.ExiledAsCost, CardSubject.Voted {

    /** Whether this subject declares a target, chosen as the spell or ability is put on the stack. */
    default boolean declaresTarget() {
        return false;
    }

    /** The target an effect whose cards this subject names declares. */
    default TargetSpec targetSpec() {
        return declaresTarget() ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }

    /** Whether this subject can name cards in a hand or library that no step revealed (CR 400.2). */
    default boolean inHiddenZone() {
        return false;
    }

    /**
     * The cards an earlier step of the same resolution moved: for {@link StepKind#EXILE} the cards it exiled, in exile;
     * for {@link StepKind#DESTROY} and {@link StepKind#SACRIFICE} the cards the permanents it removed became in the
     * zone they went to (a token, which ceases to exist, becomes none); for {@link StepKind#DISCARD} the discarded
     * cards, in the graveyard or the public zone a replacement put them in instead; for {@link StepKind#COUNTER_SPELL}
     * the card the countered spell became in its graveyard or in exile (a copy, an ability or a card put into a hidden
     * zone becomes none); for {@link StepKind#SEEK} the sought cards, in the hand they were put into (digital only);
     * for {@link StepKind#MILL} the milled cards, in the graveyard or in exile where a replacement put them
     * (CR 701.17c); for {@link StepKind#CHOOSE} the chosen cards, in the zone they were chosen in (also a revealed
     * hand), while they are still the objects that were chosen; for {@link StepKind#REVEAL} the cards revealed from the
     * top of libraries or from hands, while each is still in that library or hand (CR 701.20b); for {@link StepKind#RETURN} the cards
     * returned to a hand, followed into that hand as a sought card is, for "it perpetually gets +3/+3" (digital only:
     * otherwise a card put into a hidden zone is no longer found, CR 400.7j); for {@link StepKind#CONJURE} the conjured
     * cards, in the graveyard, exile or library they were put into (digital only: "you may cast that card", "the
     * duplicate perpetually gets +1/+1").
     * The rest of the effect can find a card it moved to a public zone (CR 400.7j); a card that has
     * left that zone since is a new object and is not named.
     *
     * <p>With a {@code filter}, only the cards matching it as they are now: "that card" after "exile cards from the top
     * of your library until you exile a nonland card" is the one nonland card among them.
     *
     * @param kind   the step kind whose most recent step is read
     * @param filter which of the moved cards are named; {@code null} for all of them
     */
    record CardsThisWay(StepKind kind, CardPredicate filter) implements CardSubject {

        public CardsThisWay(StepKind kind) {
            this(kind, null);
        }

        public CardsThisWay {
            Objects.requireNonNull(kind, "kind");
            switch (kind) {
                case EXILE, DESTROY, SACRIFICE, DISCARD, COUNTER_SPELL, SEEK, MILL, CHOOSE, REVEAL, RETURN, CONJURE -> {
                }
                default -> throw new IllegalArgumentException("A " + kind + " step leaves no card for a card "
                        + "subject to name");
            }
        }
    }

    /**
     * "That card", "it" after "whenever a creature dies", "one of them" in "whenever you discard one or more nonland
     * cards, you may exile one of them from your graveyard": the cards the trigger event was about that match
     * {@code filter} and are still in the graveyard stay the event put them in, or, for a card the event exiled
     * ("dies or is put into exile"; a replacement exiled the sacrificed card), in that exile stay (CR 400.7). A card
     * exiled face down is named too, as exile is a public zone (CR 400.2, 400.7e), but {@code filter} sees it as a card
     * with no characteristics (CR 406.3a).
     *
     * @param filter which of the event's cards are named; {@code null} for all of them
     */
    record TriggeringCards(CardPredicate filter) implements CardSubject {
    }

    /**
     * "This card" in an ability that works from a graveyard ("you may return this card from your graveyard to the
     * battlefield"): the ability's card, while it is in the graveyard stay it was in as the ability was put on the
     * stack (CR 400.7).
     */
    record SourceCard() implements CardSubject {
    }

    /**
     * "The exiled card" of an ability whose cost exiled it: the card this ability or spell exiled to pay for itself,
     * while it is in exile (CR 400.7j, 608.2h), whatever the source imprinted or exiled before or since.
     */
    record ExiledAsCost() implements CardSubject {
    }

    /**
     * "All cards in your graveyard destroyed this way", "a creature card put into your graveyard this way": those of
     * the cards {@code cards} names that are in the controller's graveyard.
     *
     * @param cards among which cards
     */
    record InYourGraveyard(CardSubject cards) implements CardSubject {

        public InYourGraveyard {
            Objects.requireNonNull(cards, "cards");
            if (cards.declaresTarget()) {
                throw new IllegalArgumentException("A target card's graveyard is the target's scope: " + cards);
            }
        }
    }

    /**
     * "A creature card from your graveyard", "all land cards in each graveyard" where nothing is targeted: the cards
     * matching {@code filter} in the graveyards of the players {@code owners} names, as they are now.
     *
     * @param owners whose graveyards
     * @param filter which cards; {@code null} for every card
     */
    record InGraveyards(PlayerSubject owners, CardPredicate filter) implements CardSubject {

        public InGraveyards {
            Objects.requireNonNull(owners, "owners");
        }

        @Override
        public boolean declaresTarget() {
            return owners.declaresTarget();
        }
    }

    /**
     * "A card you own in exile with a study counter on it", "an exiled card named Sengir Nosferatu": the exiled cards
     * matching {@code filter} owned by the players {@code owners} names. The filter sees a card exiled face down as a
     * card with no characteristics (CR 406.3a).
     *
     * @param owners whose cards; {@code null} for everybody's
     * @param filter which cards; {@code null} for every card
     */
    record InExile(PlayerSubject owners, CardPredicate filter) implements CardSubject {

        @Override
        public boolean declaresTarget() {
            return owners != null && owners.declaresTarget();
        }
    }

    /**
     * "The cards exiled with this artifact", "the exiled card" of a linked ability (CR 607.2a): the cards exiled with
     * the source, as the permanent it is or last was, that are still in exile; with a {@code filter}, those matching
     * it as they are now, a card exiled face down as one with no characteristics (CR 406.3a).
     *
     * @param owners "you own": whose cards of them; {@code null} for everybody's
     * @param filter which of them; {@code null} for all of them
     */
    record ExiledWithSource(PlayerSubject owners, CardPredicate filter) implements CardSubject {

        public ExiledWithSource {
            if (owners != null && owners.declaresTarget()) {
                throw new IllegalArgumentException("The effect reading the cards declares a player target: " + owners);
            }
        }
    }

    /**
     * "The rest", "each other card exiled with it": the cards {@code cards} names but those {@code excluded} names.
     *
     * @param cards    among which cards
     * @param excluded which of them aren't named
     */
    record Except(CardSubject cards, CardSubject excluded) implements CardSubject {

        public Except {
            Objects.requireNonNull(cards, "cards");
            Objects.requireNonNull(excluded, "excluded");
            if (cards.declaresTarget() || excluded.declaresTarget()) {
                throw new IllegalArgumentException("Neither part of \"the rest\" is a target");
            }
        }

        @Override
        public boolean inHiddenZone() {
            return cards.inHiddenZone();
        }
    }

    /**
     * "Target [filter] card in a graveyard": the card the effect targets in a graveyard of {@code scope}, while it is
     * still there and still matches {@code filter} (CR 608.2b). The effect reading it declares the target.
     */
    record TargetCard(CardPredicate filter, GraveyardSearchScope scope) implements CardSubject {

        public TargetCard {
            Objects.requireNonNull(scope, "scope");
        }

        @Override
        public boolean declaresTarget() {
            return true;
        }

        @Override
        public TargetSpec targetSpec() {
            return TargetSpec.benign(filter == null ? TargetPredicates.graveyardCard(scope)
                    : TargetPredicates.graveyardCards(filter, scope));
        }
    }

    /**
     * "Target [filter] card exiled with this creature", "target face-up exiled card", "target card you don't own in
     * exile": the card the effect targets in exile, while it is still there, face up, and still matches (CR 608.2b). A
     * face-down card is never one (CR 406.3a).
     *
     * @param filter           which cards; {@code null} for every card
     * @param exiledWithSource whether it has to be exiled with the source, as the permanent it is or last was
     * @param owner            whose card it has to be
     */
    record TargetExiledCard(CardPredicate filter, boolean exiledWithSource, PlayerRelation owner)
            implements CardSubject {

        public TargetExiledCard {
            Objects.requireNonNull(owner, "owner");
        }

        @Override
        public boolean declaresTarget() {
            return true;
        }

        @Override
        public TargetSpec targetSpec() {
            return TargetSpec.benign(new TargetPredicate.ExiledCards(
                    filter == null ? new CardTruePredicate() : filter, exiledWithSource, owner));
        }
    }

    /**
     * "The top card of your graveyard", "if the top card of target player's graveyard is a creature card, that card":
     * the last card put into each named player's graveyard, if it matches {@code filter}.
     *
     * @param owners whose graveyards
     * @param filter what that card has to be; {@code null} for any card
     */
    record TopOfGraveyard(PlayerSubject owners, CardPredicate filter) implements CardSubject {

        public TopOfGraveyard {
            Objects.requireNonNull(owners, "owners");
        }

        @Override
        public boolean declaresTarget() {
            return owners.declaresTarget();
        }
    }

    /**
     * "A creature card from your hand", "all land cards from it", "the cards in their hand": the cards matching
     * {@code filter} in the hands of the players {@code owners} names. A hand is hidden (CR 400.2): a player choosing
     * among them is shown only their own (CR 402.3), and a step that moves them to a hidden zone doesn't name them.
     *
     * @param owners whose hands
     * @param filter which cards; {@code null} for every card
     */
    record InHand(PlayerSubject owners, CardPredicate filter) implements CardSubject {

        public InHand {
            Objects.requireNonNull(owners, "owners");
        }

        @Override
        public boolean declaresTarget() {
            return owners.declaresTarget();
        }

        @Override
        public boolean inHiddenZone() {
            return true;
        }
    }

    /**
     * "A random instant card from your library": the cards matching {@code filter} in the libraries of the players
     * {@code owners} names. A library is hidden (CR 400.2): only a choice at random is made among them.
     *
     * @param owners whose libraries
     * @param filter which cards; {@code null} for every card
     */
    record InLibrary(PlayerSubject owners, CardPredicate filter) implements CardSubject {

        public InLibrary {
            Objects.requireNonNull(owners, "owners");
        }

        @Override
        public boolean declaresTarget() {
            return owners.declaresTarget();
        }

        @Override
        public boolean inHiddenZone() {
            return true;
        }
    }

    /**
     * "Your commander from the command zone", "this card from the command zone": the cards matching {@code filter} in
     * the command zones of the players {@code owners} names.
     *
     * @param owners whose command zones
     * @param filter which cards; {@code null} for every card
     */
    record InCommandZone(PlayerSubject owners, CardPredicate filter) implements CardSubject {

        public InCommandZone {
            Objects.requireNonNull(owners, "owners");
        }

        @Override
        public boolean declaresTarget() {
            return owners.declaresTarget();
        }
    }

    /**
     * "Each card with the most votes or tied for most votes" ({@link VoteEffect.Tally#MOST}), "each card with one or more
     * votes" ({@link VoteEffect.Tally#ANY}): the cards the most recent vote of the same resolution ({@code VoteEffect})
     * was cast for, by the votes each received, those still in the graveyard or exile they were voted for in (a card
     * that moves is a new object, CR 400.7). A step result.
     *
     * @param tally which voted cards are named
     */
    record Voted(VoteEffect.Tally tally) implements CardSubject {

        public Voted {
            Objects.requireNonNull(tally, "tally");
            if (tally == VoteEffect.Tally.NONE) {
                throw new IllegalArgumentException("The cards that received no votes are not recorded");
            }
        }
    }
}
