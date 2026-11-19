package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * Taps permanent(s) as an effect (fires "becomes tapped" triggers). The {@link PermanentSubject} names which
 * permanent(s) are tapped: {@code target()} ("tap target creature"), {@code source()} ("tap this creature"),
 * {@code attached()} ("tap enchanted creature"), {@code triggering()} ("tap that creature"), or a set such as
 * {@code controlledBy(targetPlayer(), land)} ("tap all lands target player controls"), whose filter is part of the
 * subject.
 *
 * <p>Replaces the former {@code TapTargetPermanentEffect}, {@code TapSelfEffect},
 * {@code TapEnchantedCreatureEffect}, {@code TapPermanentsOfTargetPlayerEffect},
 * {@code TapAllAttackingCreaturesEffect} and {@code TapCreaturesEffect}.
 *
 * <p>With a set subject, {@code chosenCount} of 0 taps every permanent in it; a positive {@code chosenCount} instead
 * lets the resolving controller pick <em>up to</em> that many of them to tap (Yosei, the Morning Star).
 *
 * @param subject     which permanent(s) to tap
 * @param filter      the targeting restriction of a {@code target()} / {@code allTargets()} subject (null = no
 *                    restriction); a set subject carries its own filter
 * @param chosenCount 0 = tap every permanent the subject names; &gt;0 = the controller chooses up to N of a set
 */
public record TapPermanentsEffect(PermanentSubject subject, PermanentPredicate filter, int chosenCount)
        implements CardEffect {

    public TapPermanentsEffect {
        Objects.requireNonNull(subject, "subject");
        if (filter != null && !(subject instanceof PermanentSubject.Target)
                && !(subject instanceof PermanentSubject.AllTargets)) {
            throw new IllegalArgumentException("Only a target subject takes a target filter: " + subject);
        }
        if (chosenCount < 0 || chosenCount > 0 && !subject.isSet()) {
            throw new IllegalArgumentException("Choosing up to N permanents needs a set subject: " + subject);
        }
    }

    public TapPermanentsEffect(PermanentSubject subject) {
        this(subject, null, 0);
    }

    public TapPermanentsEffect(PermanentSubject subject, PermanentPredicate filter) {
        this(subject, filter, 0);
    }

    /** "Tap up to N target permanents" taps only its targets, so with none chosen it does nothing. */
    @Override
    public boolean actsOnlyOnItsTargets() {
        return subject instanceof PermanentSubject.Target;
    }

    @Override
    public TargetSpec targetSpec() {
        if (subject.declaresTarget()) {
            // The single-target validation runs requireBattlefieldTarget.
            return TargetSpec.benign(TargetPredicates.permanent(), filter);
        }
        if (subject instanceof PermanentSubject.ControlledBy controlledBy && controlledBy.players().declaresTarget()) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        if (subject instanceof PermanentSubject.Source) {
            return new TargetSpec(null, false, null, true, 1);
        }
        return TargetSpec.NONE;
    }
}
