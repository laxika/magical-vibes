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
 * zone (a card that moves is a new object, CR 400.7). Six variants name cards no other player may see, {@link InHand},
 * {@link InLibrary}, {@link TopmostInLibrary}, {@link TriggeringCardsInHand}, {@link Owned} and {@link OutsideTheGame}
 * ({@link #inHiddenZone()}): they say nothing about who may see those cards. {@link Owned}, {@link CardsOf} and
 * {@link SourceObjectCard} also name the cards of permanents and spells, so only the perpetual verbs and a verb that
 * looks at the zone it is given (a graveyard cast permission, a put onto the battlefield) read them.
 *
 * <p>{@link TargetCard} and {@link TargetExiledCard} declare a card target, and a variant of a target player's cards
 * that player ({@link #declaresTarget()}, {@link #targetSpec()}). A static ability can use {@link InExile},
 * {@link ExiledWithSource}, {@link InGraveyards}, {@link TopmostInLibrary} and {@link ExiledAsCost} with players it
 * names without a stack entry ({@code SubjectResolutionService.namedCards}); every other variant reads a stack entry.
 *
 * <p>The variants and the wording each stands for are listed in {@code agent-docs/SUBJECTS_REFERENCE.md}.
 */
public sealed interface CardSubject permits CardSubject.CardsThisWay, CardSubject.TargetCard,
        CardSubject.TriggeringCards, CardSubject.TriggeringCardsInHand, CardSubject.SourceCard, CardSubject.InYourGraveyard,
        CardSubject.OwnedBy,
        CardSubject.InGraveyards, CardSubject.InExile, CardSubject.ExiledWithSource, CardSubject.Except,
        CardSubject.TargetExiledCard, CardSubject.TopOfGraveyard, CardSubject.InHand, CardSubject.InLibrary,
        CardSubject.TopmostInLibrary, CardSubject.InCommandZone, CardSubject.ExiledAsCost, CardSubject.Voted,
        CardSubject.Owned, CardSubject.OutsideTheGame, CardSubject.TargetGraveyardCards, CardSubject.CardsOf,
        CardSubject.SourceObjectCard {

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
     * zone becomes none); for {@link StepKind#SEEK} the sought cards, in the hand, graveyard or exile they were put into (digital only);
     * for {@link StepKind#MILL} the milled cards, in the graveyard or in exile where a replacement put them
     * (CR 701.17c); for {@link StepKind#CHOOSE} the chosen cards, in the zone they were chosen in (also a revealed
     * hand), while they are still the objects that were chosen; for {@link StepKind#REVEAL} the cards revealed from the
     * top of libraries or from hands, while each is still in that library or hand (CR 701.20b); for {@link StepKind#RETURN} the cards
     * returned to a hand, followed into that hand as a sought card is, for "it perpetually gets +3/+3" (digital only:
     * otherwise a card put into a hidden zone is no longer found, CR 400.7j); for {@link StepKind#CONJURE} the conjured
     * cards, in the graveyard, exile or library they were put into (digital only: "you may cast that card", "the
     * duplicate perpetually gets +1/+1"); for {@link StepKind#SEARCH} the cards a library search found, in the hand,
     * graveyard (or exile, where a replacement put a graveyard card instead) or exile it put them in, or in the library
     * for a search that reveals and keeps them there ("put that card into your hand", CR 701.20b); a card it put onto
     * the battlefield is {@code Subjects.searched()}.
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
                case EXILE, DESTROY, SACRIFICE, DISCARD, COUNTER_SPELL, SEEK, MILL, CHOOSE, REVEAL, RETURN, CONJURE, SEARCH -> {
                }
                default -> throw new IllegalArgumentException("A " + kind + " step leaves no card for a card "
                        + "subject to name");
            }
        }

        /**
         * Sought cards are random cards of a library put into a hand that nothing revealed (CR 400.2): "choose one of
         * them" is a choice among hidden cards.
         */
        @Override
        public boolean inHiddenZone() {
            return kind == StepKind.SEEK;
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
     * <p>{@code asLastKnown}: "an artifact or creature card from among those cards" after "whenever one or more cards
     * leave your graveyard": the event's cards wherever they are, each one that has left the zone the event put it in
     * (or, for a departure, the zone it left) as the event recorded it, its last known information (CR 608.2h), for a
     * step that needs only information about the card (a choice and a copy of it).
     *
     * @param filter      which of the event's cards are named; {@code null} for all of them
     * @param asLastKnown whether a card that has left its zone is named as it last existed there
     */
    record TriggeringCards(CardPredicate filter, boolean asLastKnown) implements CardSubject {

        public TriggeringCards(CardPredicate filter) {
            this(filter, false);
        }
    }

    /**
     * "That card", "those cards" of an event that put cards into a hand: "whenever a Kithkin card is put into your hand
     * from your library", "whenever a card in your graveyard is put into your hand", "whenever you seek one or more
     * cards": the event's cards, each while it is still in the hand it was put into (CR 400.7). A hand is hidden
     * (CR 400.2), so a step that only needs information about such a card ("conjure a duplicate of that card") reads it
     * as the event last knew it, also once it has left the hand (CR 608.2h; {@code SubjectResolutionService
     * .cardsToDuplicate}).
     */
    record TriggeringCardsInHand() implements CardSubject {

        @Override
        public boolean inHiddenZone() {
            return true;
        }
    }

    /**
     * "This card" in an ability that works from a graveyard or from exile ("you may return this card from your
     * graveyard to the battlefield", "remove an egg counter from it. Then if this card has no egg counters on it, return
     * it to the battlefield"): the ability's card, while it is in the graveyard or exile stay it was in as the ability
     * was put on the stack (CR 400.7). A face-down card in exile is named too: it is the ability's own card.
     */
    record SourceCard() implements CardSubject {
    }

    /**
     * "This card", "this creature", "it" of the ability's own object in whichever zone the ability refers to it: the
     * source permanent's own card while it is that permanent, the card in the graveyard or exile stay its ability works
     * from or its dies trigger found it in (CR 400.7e), the card in the hand its ability was activated from, or the
     * permanent this resolution returned it as (CR 400.7j); nothing once it has changed zones since (CR 400.7). Unlike
     * {@link SourceCard} it never names the card a battlefield ability's permanent became in a graveyard; a copy's
     * ability names the copying card. Only for the perpetual verbs, "you may cast it from your graveyard this turn"
     * (Ichor Aberration), which grants the permission only to a card it names in a graveyard, and "return this card / it
     * to the battlefield" ({@link PutCardsOntoBattlefieldEffect}), which puts only a card it names outside the
     * battlefield.
     */
    record SourceObjectCard() implements CardSubject {
    }

    /**
     * "The exiled card" of an ability whose cost exiled it: the card this ability or spell exiled to pay for itself,
     * while it is in exile (CR 400.7j, 608.2h), whatever the source imprinted or exiled before or since; "the exiled
     * cards" of an activated ability whose cost exiled several from a graveyard (Coin of Fate), those still in exile.
     * For a static ability of a permanent, the cards exiled to pay the cost of the spell it was with delve (CR 702.66a):
     * "if a creature card with flying was exiled with this creature's delve ability" asks about the exile, so a card
     * that has left exile since still counts (Soulflayer).
     */
    record ExiledAsCost() implements CardSubject {
    }

    /**
     * "That creature perpetually gets +1/+0", "it perpetually gains haste" of a permanent or a spell (digital only): the
     * physical card of each permanent {@code permanents} names while it is still that permanent on the battlefield (its
     * own card, which a copy effect doesn't change), or, for {@code Subjects.triggeringSpell()}, the card of the spell
     * the trigger event was about while that spell is still on the stack (no copy of a spell, which is no card,
     * CR 707.10). A permanent or spell that has left is a new object (CR 400.7), so nothing is named for it. Only for
     * the perpetual verbs, which change the cards themselves: it names cards no mover may move.
     *
     * @param permanents the permanents (or the triggering spell) whose cards are named; it declares no target
     */
    record CardsOf(PermanentSubject permanents) implements CardSubject {

        public CardsOf {
            Objects.requireNonNull(permanents, "permanents");
            if (permanents.declaresTarget()) {
                throw new IllegalArgumentException("A perpetual change to a target's card names it by its own subject: "
                        + permanents);
            }
        }
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
     * "The card you discarded" among the cards each player discarded (Creeping Dread): those of the cards {@code cards}
     * names that a player {@code owners} names owns, in whatever zone {@code cards} finds them (a discarded card a
     * replacement exiled instead too, which {@link InYourGraveyard} leaves out).
     *
     * @param cards  among which cards
     * @param owners whose cards
     */
    record OwnedBy(CardSubject cards, PlayerSubject owners) implements CardSubject {

        public OwnedBy {
            Objects.requireNonNull(cards, "cards");
            Objects.requireNonNull(owners, "owners");
            if (cards.declaresTarget() || owners.declaresTarget()) {
                throw new IllegalArgumentException("Cards narrowed by owner declare no target: " + cards + ", "
                        + owners);
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
     * it as they are now, a card exiled face down as one with no characteristics (CR 406.3a). With an
     * {@code abilityLink}, only the cards the source's ability of that link exiled: an ability the source gained
     * exiles under a link of its own, so "creature cards exiled with it" doesn't read them (CR 607.2a, 607.5). With
     * {@link #UNLINKED}, only the cards an exiling ability that records no link exiled: the source's own imprint or
     * "exile target card" ability, not one it has as a copy (Dermotaxi's ruling).
     *
     * @param owners      "you own": whose cards of them; {@code null} for everybody's
     * @param filter      which of them; {@code null} for all of them
     * @param abilityLink the link the exiling ability records on each card it exiles; {@link #UNLINKED} for the cards
     *                    exiled with no link; {@code null} for every card exiled with the source
     */
    record ExiledWithSource(PlayerSubject owners, CardPredicate filter, String abilityLink) implements CardSubject {

        /** The {@code abilityLink} naming the cards exiled with the source under no link. */
        public static final String UNLINKED = "unlinked";

        public ExiledWithSource {
            if (owners != null && owners.declaresTarget()) {
                throw new IllegalArgumentException("The effect reading the cards declares a player target: " + owners);
            }
        }

        public ExiledWithSource(PlayerSubject owners, CardPredicate filter) {
            this(owners, filter, null);
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
     * "Up to three target cards from a single graveyard", "up to four target cards in your graveyard": every card the
     * spell targets in a graveyard of {@code scope}, while it is still there and matches {@code filter}; an illegal
     * target is not named (CR 608.2b). It declares no target: the card declares the targets
     * ({@code target(new GraveyardCardPredicateTargetFilter(filter, scope), 0, maxTargets)}), and as the spell is cast
     * it is how the cast path knows to ask for up to {@code maxTargets} of them, from one graveyard where
     * {@code singleGraveyard} says so. So several steps of the spell can read the same targets: a put, a choice among
     * them, and "the rest" ({@link Except}). The steps that can name it are {@code PutCardsIntoLibraryEffect},
     * {@code PutCardsIntoHandEffect} and {@code ChooseCardEffect} (found by
     * {@code EffectResolution.targetGraveyardCards}).
     *
     * @param filter          which cards; {@code null} for every card
     * @param scope           whose graveyards
     * @param maxTargets      how many may be chosen at most
     * @param singleGraveyard whether all of them come from one graveyard
     */
    record TargetGraveyardCards(CardPredicate filter, GraveyardSearchScope scope, int maxTargets,
                                boolean singleGraveyard) implements CardSubject {

        public TargetGraveyardCards {
            Objects.requireNonNull(scope, "scope");
            if (maxTargets < 1) {
                throw new IllegalArgumentException("Up to how many target cards: " + maxTargets);
            }
        }

        /** "From a single graveyard". */
        public TargetGraveyardCards fromASingleGraveyard() {
            return new TargetGraveyardCards(filter, scope, maxTargets, true);
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
     * "The topmost creature card in your library", "the top land card in your library": in the library of each player
     * {@code owners} names, the card nearest the top that matches {@code filter} as it is now, if one does. A library
     * is hidden (CR 400.2): nobody is shown which card it is.
     *
     * @param owners whose libraries
     * @param filter what that card has to be; {@code null} for the top card
     */
    record TopmostInLibrary(PlayerSubject owners, CardPredicate filter) implements CardSubject {

        public TopmostInLibrary {
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
     * "Cards you own from outside the game": the matching cards the players {@code owners} names own outside the game
     * (their sideboards; CR 400.11), judged as they are, since no effect of the game applies to them. Nobody else may
     * see them, so a choice among them names none.
     *
     * @param owners whose cards
     * @param filter which cards; {@code null} for every card
     */
    record OutsideTheGame(PlayerSubject owners, CardPredicate filter) implements CardSubject {

        public OutsideTheGame {
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
     * "All creature cards you own with mana value 2 or less", "each card you own with {X} in its mana cost" (digital
     * only): every card matching {@code filter} that a player {@code owners} names owns, in every zone: library,
     * hand, graveyard, command zone, exile, the battlefield (a nontoken permanent's card, judged as the permanent is
     * now and named as its own physical card, which a copy effect doesn't change) and the stack (a spell's card; no
     * copy of a spell, which is no card, CR 707.10). Only for the perpetual verbs, which change the cards themselves:
     * it names cards no mover may move.
     *
     * @param owners whose cards
     * @param filter which cards; {@code null} for every card
     */
    record Owned(PlayerSubject owners, CardPredicate filter) implements CardSubject {

        public Owned {
            Objects.requireNonNull(owners, "owners");
            if (owners.declaresTarget()) {
                throw new IllegalArgumentException("The effect reading the cards declares a player target: " + owners);
            }
        }

        @Override
        public boolean inHiddenZone() {
            return true;
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
