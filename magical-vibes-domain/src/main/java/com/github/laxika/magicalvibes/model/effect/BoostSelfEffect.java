package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.Set;

/**
 * The permanent {@code subject} names gets +X/+Y, and gains {@code keywords}, for {@code duration}: "this creature
 * gets +2/+2 until end of turn" ({@link Subjects#source()}, the default), "equipped creature gets +1/+0"
 * ({@link Subjects#attached()}), "whenever a creature you control becomes blocked, it gets +1/+1"
 * ({@link Subjects#triggering()}), and the other non-targeting subjects of one permanent. A targeted pump is
 * {@link BoostTargetCreatureEffect}.
 *
 * <p>The amounts are {@link DynamicAmount}s evaluated with the boosted permanent as the amount source, so "it gets
 * +1/+1 for each other creature you control" and "double its power" read the boosted creature. As a one-shot effect
 * each change is a floating effect on that permanent with {@code duration} (CR 611.2a); in the {@code STATIC} slot
 * only the {@code source()} form without keywords is a continuous self bonus.
 */
public record BoostSelfEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost,
                              Duration duration, PermanentSubject subject, Set<Keyword> keywords) implements CardEffect {

    public BoostSelfEffect {
        subject = subject == null ? Subjects.source() : subject;
        keywords = keywords == null ? Set.of() : Set.copyOf(keywords);
        if (subject.declaresTarget() || subject.isSet()) {
            throw new IllegalArgumentException("BoostSelfEffect names one permanent without targeting it: " + subject);
        }
    }

    public BoostSelfEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost, Duration duration) {
        this(powerBoost, toughnessBoost, duration, Subjects.source(), Set.of());
    }

    public BoostSelfEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost) {
        this(powerBoost, toughnessBoost, Duration.UNTIL_END_OF_TURN);
    }

    /** Convenience for plain fixed boosts ("gets +2/+2 until end of turn"). */
    public BoostSelfEffect(int powerBoost, int toughnessBoost) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), Duration.UNTIL_END_OF_TURN);
    }

    public BoostSelfEffect(int powerBoost, int toughnessBoost, Duration duration) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), duration);
    }

    /** "Equipped creature gets +X/+Y until end of turn", "it gets +X/+Y until end of turn". */
    public BoostSelfEffect(PermanentSubject subject, DynamicAmount powerBoost, DynamicAmount toughnessBoost) {
        this(powerBoost, toughnessBoost, Duration.UNTIL_END_OF_TURN, subject, Set.of());
    }

    /** "It gets +X/+Y and gains trample until end of turn". */
    public BoostSelfEffect(PermanentSubject subject, DynamicAmount powerBoost, DynamicAmount toughnessBoost,
                           Set<Keyword> keywords) {
        this(powerBoost, toughnessBoost, Duration.UNTIL_END_OF_TURN, subject, keywords);
    }

    public BoostSelfEffect(PermanentSubject subject, int powerBoost, int toughnessBoost) {
        this(subject, new Fixed(powerBoost), new Fixed(toughnessBoost));
    }

    public BoostSelfEffect(PermanentSubject subject, int powerBoost, int toughnessBoost, Keyword keyword) {
        this(subject, new Fixed(powerBoost), new Fixed(toughnessBoost), Set.of(keyword));
    }

    /** The self-targeting marker for the source (and the source-or-host) form; any other subject declares nothing. */
    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Source || subject instanceof PermanentSubject.AttachedOrSource
                ? new TargetSpec(null, false, null, true, 1)
                : TargetSpec.NONE;
    }

    /**
     * An Aura's or Equipment's ability boosting its host captures that host as it is activated, before a sacrifice or
     * return cost severs the attachment ("Sacrifice this Aura: Enchanted creature gets +3/+3", Briar Shield).
     */
    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return subject instanceof PermanentSubject.Attached || subject instanceof PermanentSubject.AttachedOrSource;
    }

    @Override
    public boolean hasAbilityResolutionCondition() {
        return powerBoost.requiresAbilityResolutionCount() || toughnessBoost.requiresAbilityResolutionCount();
    }
}
