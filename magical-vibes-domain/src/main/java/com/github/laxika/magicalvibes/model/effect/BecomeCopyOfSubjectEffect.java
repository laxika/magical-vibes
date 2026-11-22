package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "This creature becomes a copy of that card [until end of turn] [, except it has this ability]": the source
 * permanent becomes a copy of the card {@code card} names, as it is ({@code cardsThisWay(EXILE)}: the card in exile),
 * or of the permanent {@code permanent} names, as it is or, once an earlier step moved it, by the copiable values it
 * last had on the battlefield (CR 707.2, 608.2h: "becomes a copy of that creature" after exiling it, Identity Thief).
 *
 * <p>It declares no target: the step that moved the card or permanent does. Nothing happens once the source has left
 * the battlefield.
 *
 * @param card            the card copied; {@code null} when {@code permanent} names what is copied
 * @param permanent       the permanent copied; {@code null} when {@code card} names what is copied
 * @param duration        {@link Duration#PERMANENT} for no stated duration, or {@link Duration#UNTIL_END_OF_TURN}
 * @param keepsThisAbility "except it has this ability": the copy has each ability of the source, as it was, that this
 *                        effect is part of
 */
public record BecomeCopyOfSubjectEffect(CardSubject card, PermanentSubject permanent, Duration duration,
                                        boolean keepsThisAbility) implements CardEffect {

    public BecomeCopyOfSubjectEffect {
        Objects.requireNonNull(duration, "duration");
        if ((card == null) == (permanent == null)) {
            throw new IllegalArgumentException("Either a card or a permanent is copied");
        }
        if (card != null && card.declaresTarget()) {
            throw new IllegalArgumentException("The step that moved the card declares the target: " + card);
        }
        if (!duration.equals(Duration.PERMANENT) && !duration.equals(Duration.UNTIL_END_OF_TURN)) {
            throw new IllegalArgumentException("No copy of a subject lasts " + duration);
        }
    }

    /** "Becomes a copy of that card." */
    public static BecomeCopyOfSubjectEffect of(CardSubject card) {
        return new BecomeCopyOfSubjectEffect(card, null, Duration.PERMANENT, false);
    }

    /** "Becomes a copy of that creature." */
    public static BecomeCopyOfSubjectEffect of(PermanentSubject permanent) {
        return new BecomeCopyOfSubjectEffect(null, permanent, Duration.PERMANENT, false);
    }

    /** This copy "until end of turn". */
    public BecomeCopyOfSubjectEffect untilEndOfTurn() {
        return new BecomeCopyOfSubjectEffect(card, permanent, Duration.UNTIL_END_OF_TURN, keepsThisAbility);
    }

    /** This copy "except it has this ability". */
    public BecomeCopyOfSubjectEffect keepingThisAbility() {
        return new BecomeCopyOfSubjectEffect(card, permanent, duration, true);
    }
}
