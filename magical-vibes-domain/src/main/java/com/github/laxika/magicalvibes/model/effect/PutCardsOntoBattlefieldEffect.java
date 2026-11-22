package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * "Put those cards onto the battlefield", "return that card to the battlefield": each card {@code cards} names, in
 * whichever zone it names it in, enters under its owner's control, or under that of {@code controller}, all together,
 * an Aura attached as that player chooses (CR 303.4f). A card that can't enter, or an Aura with nothing to enchant,
 * stays where it is (CR 303.4g). "Puts all land cards revealed this way onto the battlefield tapped" is
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
     * @param counters how many of those counters
     * @param grants   continuous effects without a duration on it, each reading {@code Subjects.target()}, in effect
     *                 as it enters ("that creature is a black Zombie in addition to its other colors and types",
     *                 "she's a land named Moon")
     */
    public record EnteringAs(FaceDown faceDown, CounterType counter, int counters, List<CardEffect> grants) {

        static final EnteringAs NOTHING_ELSE = new EnteringAs(null, null, 0, List.of());

        public EnteringAs {
            grants = List.copyOf(grants);
        }
    }

    /** A face-down permanent's characteristics: "It's a 2/2 Cyberman artifact creature", "It's a Forest land". */
    public record FaceDown(int power, int toughness, Set<CardType> types, Set<CardSubtype> subtypes) {

        public FaceDown {
            types = Set.copyOf(types);
            subtypes = Set.copyOf(subtypes);
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
                new EnteringAs(new FaceDown(power, toughness, types, subtypes), as.counter(), as.counters(), as.grants()));
    }

    /** These cards entering face down as 2/2 creatures (CR 708.2a). */
    public PutCardsOntoBattlefieldEffect faceDown() {
        return faceDown(2, 2, Set.of(CardType.CREATURE), Set.of());
    }

    /** These cards each entering with {@code count} counters of that kind. */
    public PutCardsOntoBattlefieldEffect withCounters(CounterType counter, int count) {
        EnteringAs as = enteringAs();
        return new PutCardsOntoBattlefieldEffect(cards, tapped, controller,
                new EnteringAs(as.faceDown(), counter, count, as.grants()));
    }

    /** These cards each entering with these effects without a duration on it, each over {@code Subjects.target()}. */
    public PutCardsOntoBattlefieldEffect enteringWith(CardEffect... grants) {
        EnteringAs as = enteringAs();
        return new PutCardsOntoBattlefieldEffect(cards, tapped, controller,
                new EnteringAs(as.faceDown(), as.counter(), as.counters(), List.of(grants)));
    }

    private EnteringAs enteringAs() {
        return entering == null ? EnteringAs.NOTHING_ELSE : entering;
    }
}
