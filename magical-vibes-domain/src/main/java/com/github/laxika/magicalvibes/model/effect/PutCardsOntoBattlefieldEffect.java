package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * "Put those cards onto the battlefield", "return that card to the battlefield": each card {@code cards} names, in
 * whichever zone it names it in, enters under its owner's control, or under that of {@code controller}, all together,
 * an Aura attached as that player chooses (CR 303.4f). A card that can't enter, or an Aura with nothing to enchant,
 * stays where it is (CR 303.4g), as does an instant or sorcery card unless it enters face down (CR 400.4a). What
 * entered is the step's {@code StepKind.RETURN} ("for each card returned this way"). "Puts all land cards revealed this way
 * onto the battlefield tapped" is
 * {@code cardsThisWay(StepKind.REVEAL, land)} (Clear the Land); "return it to the battlefield face down" after
 * "whenever a creature dies" is {@code triggeringCards()} with {@link #faceDown}.
 *
 * <p>An effect that says neither "under its owner's control" nor "under your control" puts them under yours
 * (CR 110.2a): that is {@code underControlOf(you())} unless the cards are necessarily your own.
 *
 * @param cards      the cards put onto the battlefield
 * @param tapped     whether they enter tapped
 * @param controller under whose control they enter; {@code null} for each its owner's
 * @param entering   what they are as they enter beyond the cards; {@code null} for nothing
 */
public record PutCardsOntoBattlefieldEffect(CardSubject cards, boolean tapped, PlayerSubject controller,
                                            EnteringAs entering) implements CardEffect {

    /**
     * What a permanent put onto the battlefield is as it enters, beyond its card.
     *
     * @param faceDown its face-down characteristics (CR 708.2); {@code null} for face up
     * @param counter  a kind of counter it enters with; {@code null} for none
     * @param counters how many of those counters, worked out once as the effect is applied (CR 608.2h): "with one fewer
     *                 revival counter on it" of a delayed return reads the number its maker noted
     * @param host     what it enters attached to ("attached to a creature that shares a creature type with that
     *                 creature", "attached to you"); {@code null} where the effect names nothing (CR 303.4f)
     * @param grants   continuous effects without a duration on it, each reading {@code Subjects.target()}, in effect
     *                 as it enters ("that creature is a black Zombie in addition to its other colors and types",
     *                 "she's a land named Moon")
     */
    public record EnteringAs(FaceDown faceDown, CounterType counter, DynamicAmount counters, List<CardEffect> grants,
                             AttachedTo host) {

        static final EnteringAs NOTHING_ELSE = new EnteringAs(null, null, new Fixed(0), List.of(), null);

        public EnteringAs {
            Objects.requireNonNull(counters, "counters");
            grants = List.copyOf(grants);
        }

        /** This, with its counter count worked out as {@code count}. */
        public EnteringAs withCounterCount(int count) {
            return new EnteringAs(faceDown, counter, new Fixed(count), grants, host);
        }

        /**
         * How many counters it enters with, once the effect putting it there has worked the count out
         * ({@link #withCounterCount}).
         */
        public int counterCount() {
            if (!(counters instanceof Fixed(int value))) {
                throw new IllegalStateException("The counter count is worked out as the effect is applied: " + counters);
            }
            return value;
        }
    }

    /**
     * What an Aura put onto the battlefield enters attached to: one of these, as whoever it enters under the control
     * of chooses among those it can enchant (CR 303.4f), and with none it stays where it is (CR 303.4g). A subject
     * that names one permanent or player leaves no choice.
     */
    public record AttachedTo(PermanentSubject permanents, PlayerSubject players) {

        public AttachedTo {
            if ((permanents == null) == (players == null)) {
                throw new IllegalArgumentException("An Aura enters attached to a permanent or to a player");
            }
            if (permanents != null && permanents.declaresTarget() || players != null && players.declaresTarget()) {
                throw new IllegalArgumentException("What it enters attached to is no target");
            }
        }
    }

    /**
     * A face-down permanent's characteristics: "It's a 2/2 Cyberman artifact creature", "It's a Forest land".
     *
     * @param manifested whether the card is manifested (CR 701.40a): a manifested creature card may be turned face up
     *                   for its mana cost (CR 701.40b)
     */
    public record FaceDown(int power, int toughness, Set<CardType> types, Set<CardSubtype> subtypes,
                           boolean manifested) {

        public FaceDown {
            types = Set.copyOf(types);
            subtypes = Set.copyOf(subtypes);
        }

        public FaceDown(int power, int toughness, Set<CardType> types, Set<CardSubtype> subtypes) {
            this(power, toughness, types, subtypes, false);
        }
    }

    public PutCardsOntoBattlefieldEffect {
        Objects.requireNonNull(cards, "cards");
        if (cards.declaresTarget()) {
            throw new IllegalArgumentException("The step names the cards an earlier step moved or revealed, the "
                    + "trigger event's or the source: " + cards);
        }
        if (controller != null && controller.declaresTarget()) {
            throw new IllegalArgumentException("Whose control they enter under is no target: " + controller);
        }
    }

    public PutCardsOntoBattlefieldEffect(CardSubject cards, boolean tapped) {
        this(cards, tapped, null, null);
    }

    public PutCardsOntoBattlefieldEffect(CardSubject cards) {
        this(cards, false);
    }

    /** These cards entering tapped. */
    public PutCardsOntoBattlefieldEffect enteringTapped() {
        return new PutCardsOntoBattlefieldEffect(cards, true, controller, entering);
    }

    /** These cards entering under the control of the player {@code player} names ("under your control"). */
    public PutCardsOntoBattlefieldEffect underControlOf(PlayerSubject player) {
        return new PutCardsOntoBattlefieldEffect(cards, tapped, player, entering);
    }

    /** These cards entering face down as a permanent with these characteristics. */
    public PutCardsOntoBattlefieldEffect faceDown(int power, int toughness, Set<CardType> types,
                                                  Set<CardSubtype> subtypes) {
        EnteringAs as = enteringAs();
        return new PutCardsOntoBattlefieldEffect(cards, tapped, controller,
                new EnteringAs(new FaceDown(power, toughness, types, subtypes), as.counter(), as.counters(), as.grants(),
                        as.host()));
    }

    /** These cards entering face down as 2/2 creatures (CR 708.2a). */
    public PutCardsOntoBattlefieldEffect faceDown() {
        return faceDown(2, 2, Set.of(CardType.CREATURE), Set.of());
    }

    /** "Manifest [those cards]": each entering face down as a manifested 2/2 creature (CR 701.40a). */
    public PutCardsOntoBattlefieldEffect manifested() {
        EnteringAs as = enteringAs();
        return new PutCardsOntoBattlefieldEffect(cards, tapped, controller,
                new EnteringAs(new FaceDown(2, 2, Set.of(CardType.CREATURE), Set.of(), true), as.counter(),
                        as.counters(), as.grants(), as.host()));
    }

    /** These cards each entering with {@code count} counters of that kind. */
    public PutCardsOntoBattlefieldEffect withCounters(CounterType counter, int count) {
        return withCounters(counter, new Fixed(count));
    }

    /**
     * These cards each entering with that many counters of that kind, the number worked out once as the effect is
     * applied (CR 608.2h).
     */
    public PutCardsOntoBattlefieldEffect withCounters(CounterType counter, DynamicAmount count) {
        EnteringAs as = enteringAs();
        return new PutCardsOntoBattlefieldEffect(cards, tapped, controller,
                new EnteringAs(as.faceDown(), counter, count, as.grants(), as.host()));
    }

    /** These cards each entering with these effects without a duration on it, each over {@code Subjects.target()}. */
    public PutCardsOntoBattlefieldEffect enteringWith(CardEffect... grants) {
        EnteringAs as = enteringAs();
        return new PutCardsOntoBattlefieldEffect(cards, tapped, controller,
                new EnteringAs(as.faceDown(), as.counter(), as.counters(), List.of(grants), as.host()));
    }

    /** These Aura cards each entering attached to one of the permanents {@code hosts} names. */
    public PutCardsOntoBattlefieldEffect attachedTo(PermanentSubject hosts) {
        return attachedTo(new AttachedTo(hosts, null));
    }

    /** These Aura cards each entering attached to one of the players {@code hosts} names ("attached to you"). */
    public PutCardsOntoBattlefieldEffect attachedTo(PlayerSubject hosts) {
        return attachedTo(new AttachedTo(null, hosts));
    }

    private PutCardsOntoBattlefieldEffect attachedTo(AttachedTo host) {
        EnteringAs as = enteringAs();
        return new PutCardsOntoBattlefieldEffect(cards, tapped, controller,
                new EnteringAs(as.faceDown(), as.counter(), as.counters(), as.grants(), host));
    }

    private EnteringAs enteringAs() {
        return entering == null ? EnteringAs.NOTHING_ELSE : entering;
    }
}
