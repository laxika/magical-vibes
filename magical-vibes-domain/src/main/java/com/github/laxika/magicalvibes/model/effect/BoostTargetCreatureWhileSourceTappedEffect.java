package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.Set;

/**
 * "Target creature gets +power/+toughness and gains [keywords] for as long as the source permanent
 * remains tapped." (Tawnos's Weaponry for the boost, Hisoka's Guard for a pure keyword grant —
 * 0/0 with {@link Keyword#SHROUD}). Unlike {@link BoostEffect#target} (which wears off at end of turn),
 * the handler records this as a {@code WHILE_SOURCE_TAPPED} floating continuous effect keyed to the
 * source permanent: the additive +P/+T is read by the layered pass for as long as the source stays
 * tapped, and the effect is expired the moment the source becomes untapped or leaves the battlefield
 * (CR 611.2b — it does not resume if the source is tapped again).
 *
 * <p>The wrapped floating effect reuses {@link PowerToughnessBoostEffect}, which the CR
 * 613 layer reader already applies in sublayer 7c; only the floating effect's duration/source differ.
 *
 * @param subject target creature by default, or a set found once as the ability resolves (CR 611.2c:
 *                {@link Subjects#allCreatures()}, Thran Weaponry), which doesn't target
 */
public record BoostTargetCreatureWhileSourceTappedEffect(int power, int toughness,
                                                        Set<Keyword> keywords,
                                                         PermanentSubject subject) implements CreatureBoostEffect {

    public BoostTargetCreatureWhileSourceTappedEffect(int power, int toughness) {
        this(power, toughness, Set.of(), Subjects.target());
    }

    public BoostTargetCreatureWhileSourceTappedEffect(int power, int toughness, Set<Keyword> keywords) {
        this(power, toughness, keywords, Subjects.target());
    }

    /** "All creatures get +P/+T for as long as this remains tapped": the set is found as the ability resolves. */
    public BoostTargetCreatureWhileSourceTappedEffect(int power, int toughness, PermanentSubject subject) {
        this(power, toughness, Set.of(), subject);
    }

    @Override
    public DynamicAmount powerBoost() {
        return new Fixed(power);
    }

    @Override
    public DynamicAmount toughnessBoost() {
        return new Fixed(toughness);
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.declaresTarget() ? TargetSpec.benign(TargetPredicates.creature()) : TargetSpec.NONE;
    }
}
