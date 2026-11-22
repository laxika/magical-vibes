package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * Static replacement effect that doubles damage dealt by the sources {@code sources} names, optionally restricted by
 * an additional source predicate.
 *
 * @param additionalSourceFilter what else a source must match, or {@code null}
 * @param sources                {@link Subjects#creaturesYouControl()} (Gratuitous Violence), {@link Subjects#source()}
 *                               (this permanent) or {@link Subjects#attachedCreature()} ("equipped creature", whoever
 *                               controls it)
 */
public record DoubleDamageFromCreaturesEffect(PermanentPredicate additionalSourceFilter, PermanentSubject sources)
        implements SourceDamageMultiplyingEffect {

    private static final PermanentPredicate CREATURE_FILTER = new PermanentIsCreaturePredicate();
    private static final PermanentPredicate SELF_FILTER = new PermanentIsSourcePermanentPredicate();
    private static final PermanentPredicate HOST_FILTER = new PermanentAllOfPredicate(List.of(
            CREATURE_FILTER, new PermanentIsHostOfSourceAuraPredicate()));

    public DoubleDamageFromCreaturesEffect {
        if (!sources.equals(Subjects.creaturesYouControl()) && !sources.equals(Subjects.source())
                && !sources.equals(Subjects.attachedCreature())) {
            throw new IllegalArgumentException("Unsupported damage sources: " + sources);
        }
    }

    public DoubleDamageFromCreaturesEffect() {
        this(null, Subjects.creaturesYouControl());
    }

    public DoubleDamageFromCreaturesEffect(PermanentPredicate additionalSourceFilter) {
        this(additionalSourceFilter, Subjects.creaturesYouControl());
    }

    public DoubleDamageFromCreaturesEffect(PermanentSubject sources) {
        this(null, sources);
    }

    @Override
    public int damageMultiplier() {
        return 2;
    }

    @Override
    public PermanentPredicate sourceFilter() {
        PermanentPredicate named = sources instanceof PermanentSubject.Source ? SELF_FILTER
                : sources instanceof PermanentSubject.Attached ? HOST_FILTER
                : CREATURE_FILTER;
        return additionalSourceFilter == null
                ? named
                : new PermanentAllOfPredicate(List.of(named, additionalSourceFilter));
    }

    @Override
    public boolean requiresSourceControllerMatch() {
        return !(sources instanceof PermanentSubject.Attached);
    }
}
