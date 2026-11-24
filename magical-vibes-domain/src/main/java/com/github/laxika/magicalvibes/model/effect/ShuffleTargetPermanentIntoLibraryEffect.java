package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each permanent {@code subject} names is shuffled into its owner's library: by default the target permanent ("the
 * owner of target permanent shuffles it into their library", Deglamer, Oblation), else "this creature"
 * ({@link Subjects#source()}: Blitz Hellion, Cerulean Sphinx), "enchanted creature" ({@link Subjects#attached()}:
 * Dramatic Accusation), "this creature and target creature" ({@code allOf(target(), source())}: Floodpits Drowner),
 * "each nonland permanent target player controls" ({@code controlledBy(targetPlayer(), filter)}: Teferi, Temporal
 * Pilgrim) or what an earlier step named ({@code thisWay(CHOOSE)}, {@code createdThisResolution()}).
 *
 * <p>The permanents leave the battlefield as one event, then each library one of them went into is shuffled once
 * (Psychogenic Probe). A specific object that is no longer on the battlefield still has its owner's library shuffled
 * (CR 701.24c); a target that is no longer legal is not affected (CR 608.2b). The step records
 * {@code StepKind.SHUFFLE_INTO_LIBRARY}: how many permanents each owner shuffled in, so "that player" / "its owner" of
 * a later step is {@code Subjects.playersThisWay(SHUFFLE_INTO_LIBRARY)} and "if that player does" is
 * {@code HappenedThisWay(SHUFFLE_INTO_LIBRARY)}. When the next effect of the resolution also shuffles libraries
 * ("shuffle this and your graveyard into their owner's library", Elixir of Immortality), the libraries are left for
 * it to shuffle, so each is shuffled once.
 *
 * @param subject   which permanents are shuffled in; {@code null} means {@link Subjects#target()}
 * @param predicate what the target permanent must be; a subject with a target only
 * @param thenEffect an effect resolved afterwards for the shuffled permanent's controller or owner, as
 *                   {@code recipient} says, when a rider takes no player subject (Zoyowa's Justice's "that player
 *                   discovers X"); one named permanent only
 * @param recipient whose effect {@code thenEffect} is
 */
public record ShuffleTargetPermanentIntoLibraryEffect(
        PermanentSubject subject,
        PermanentPredicate predicate,
        CardEffect thenEffect,
        ThenEffectRecipient recipient
) implements CardEffect {

    public ShuffleTargetPermanentIntoLibraryEffect {
        if (subject == null) {
            subject = Subjects.target();
        }
        if (predicate != null && !subject.declaresTarget()) {
            throw new IllegalArgumentException("A target predicate needs a target subject: " + subject);
        }
        if (thenEffect != null && (recipient == null || subject.isSet() || subject instanceof PermanentSubject.AllOf)) {
            throw new IllegalArgumentException("A then-effect follows one named permanent, with a recipient: "
                    + subject);
        }
    }

    public ShuffleTargetPermanentIntoLibraryEffect() {
        this(null, null, null, null);
    }

    public ShuffleTargetPermanentIntoLibraryEffect(PermanentPredicate predicate) {
        this(null, predicate, null, null);
    }

    public ShuffleTargetPermanentIntoLibraryEffect(CardEffect thenEffect, ThenEffectRecipient recipient) {
        this(null, null, thenEffect, recipient);
    }

    /** Shuffles what {@code subject} names into its owner's library. */
    public static ShuffleTargetPermanentIntoLibraryEffect of(PermanentSubject subject) {
        return new ShuffleTargetPermanentIntoLibraryEffect(subject, null, null, null);
    }

    /** "This creature and target [predicate]": both, as one event. */
    public static ShuffleTargetPermanentIntoLibraryEffect sourceAndTarget(PermanentPredicate predicate) {
        return new ShuffleTargetPermanentIntoLibraryEffect(Subjects.allOf(Subjects.target(), Subjects.source()),
                predicate, null, null);
    }

    /** "[Its owner] shuffles this into their library and [then-effect]": the then-effect for the source's owner. */
    public static ShuffleTargetPermanentIntoLibraryEffect sourceThen(CardEffect thenEffect,
                                                                     ThenEffectRecipient recipient) {
        return new ShuffleTargetPermanentIntoLibraryEffect(Subjects.source(), null, thenEffect, recipient);
    }

    @Override
    public TargetSpec targetSpec() {
        if (subject.declaresTarget()) {
            return TargetSpec.benign(TargetPredicates.permanent(), predicate);
        }
        if (subject.declaresPlayerTarget()) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        return TargetSpec.NONE;
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return subject instanceof PermanentSubject.Attached;
    }
}
