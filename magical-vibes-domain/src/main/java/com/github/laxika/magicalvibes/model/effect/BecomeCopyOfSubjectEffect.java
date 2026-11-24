package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;
import java.util.Set;

/**
 * "[Copier] becomes a copy of [it] [for duration] [, except ...]": each permanent {@code copier} names becomes a copy of
 * the card {@code card} names, as it is ({@code cardsThisWay(EXILE)}: the card in exile), or of the permanent
 * {@code permanent} names, by its copiable values as it is or, once it has left the battlefield, as it last existed
 * there (CR 707.2, 608.2h: "becomes a copy of that creature" after exiling it, Identity Thief). A face-down permanent
 * is copied by its face-down values (CR 708.2a); a permanent that is itself a copy is copied as that copy (CR 707.3).
 *
 * <p>The copier is {@link Subjects#source()} ("this creature becomes a copy of"), a target ("target Shapeshifter
 * becomes a copy of target creature"), the triggering permanent ("that creature becomes a copy of this creature") or a
 * set ("each other token you control"); it is found once as the effect applies (CR 608.2h), and a copier that left the
 * battlefield is not affected. It declares a target only for a target subject; the step that moved a copied card
 * declares that card's.
 *
 * @param card             the card copied; {@code null} when {@code permanent} names what is copied
 * @param permanent        the permanent copied; {@code null} when {@code card} names what is copied
 * @param copier           which permanents become copies
 * @param duration         {@link Duration#PERMANENT} for no stated duration, {@link Duration#UNTIL_END_OF_TURN},
 *                         {@link Duration#UNTIL_YOUR_NEXT_TURN}, or {@link Duration#WHILE_REMAINS_ATTACHED} ("for as
 *                         long as this Equipment remains attached to it", which does nothing once the source is no
 *                         longer attached to the copier as it resolves, CR 611.2b)
 * @param keepsThisAbility "except it has this ability": the copy has each ability of the source, as it was, that this
 *                         effect is part of; only for the source as copier
 * @param exceptions       the copy's other exceptions, part of its copiable values (CR 707.9b)
 */
public record BecomeCopyOfSubjectEffect(CardSubject card, PermanentSubject permanent, PermanentSubject copier,
                                        Duration duration, boolean keepsThisAbility,
                                        CopyExceptions exceptions) implements CardEffect {

    private static final Set<Duration> DURATIONS = Set.of(Duration.PERMANENT, Duration.UNTIL_END_OF_TURN,
            Duration.UNTIL_YOUR_NEXT_TURN, Duration.WHILE_REMAINS_ATTACHED);

    public BecomeCopyOfSubjectEffect {
        Objects.requireNonNull(duration, "duration");
        Objects.requireNonNull(copier, "copier");
        Objects.requireNonNull(exceptions, "exceptions");
        if ((card == null) == (permanent == null)) {
            throw new IllegalArgumentException("Either a card or a permanent is copied");
        }
        if (card != null && card.declaresTarget()) {
            throw new IllegalArgumentException("The step that moved the card declares the target: " + card);
        }
        if (!DURATIONS.contains(duration)) {
            throw new IllegalArgumentException("No copy of a subject lasts " + duration);
        }
        if (keepsThisAbility && !(copier instanceof PermanentSubject.Source)) {
            throw new IllegalArgumentException("Only the source keeps the ability that made it a copy");
        }
    }

    public BecomeCopyOfSubjectEffect(CardSubject card, PermanentSubject permanent, Duration duration,
                                     boolean keepsThisAbility) {
        this(card, permanent, Subjects.source(), duration, keepsThisAbility, CopyExceptions.NONE);
    }

    @Override
    public TargetSpec targetSpec() {
        return permanent != null && permanent.declaresTarget() || copier.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.permanent()) : TargetSpec.NONE;
    }

    /** "Becomes a copy of that card." */
    public static BecomeCopyOfSubjectEffect of(CardSubject card) {
        return new BecomeCopyOfSubjectEffect(card, null, Duration.PERMANENT, false);
    }

    /** "Becomes a copy of that creature." */
    public static BecomeCopyOfSubjectEffect of(PermanentSubject permanent) {
        return new BecomeCopyOfSubjectEffect(null, permanent, Duration.PERMANENT, false);
    }

    /** This copy made by {@code copier} instead of the source ("that creature becomes a copy of this creature"). */
    public BecomeCopyOfSubjectEffect by(PermanentSubject copier) {
        return new BecomeCopyOfSubjectEffect(card, permanent, copier, duration, keepsThisAbility, exceptions);
    }

    /** This copy "until end of turn". */
    public BecomeCopyOfSubjectEffect untilEndOfTurn() {
        return lasting(Duration.UNTIL_END_OF_TURN);
    }

    /** This copy for {@code duration}. */
    public BecomeCopyOfSubjectEffect lasting(Duration duration) {
        return new BecomeCopyOfSubjectEffect(card, permanent, copier, duration, keepsThisAbility, exceptions);
    }

    /** This copy "except it has this ability". */
    public BecomeCopyOfSubjectEffect keepingThisAbility() {
        return new BecomeCopyOfSubjectEffect(card, permanent, copier, duration, true, exceptions);
    }

    /** This copy "except ..." ({@link CopyExceptions}). */
    public BecomeCopyOfSubjectEffect except(CopyExceptions exceptions) {
        return new BecomeCopyOfSubjectEffect(card, permanent, copier, duration, keepsThisAbility, exceptions);
    }
}
