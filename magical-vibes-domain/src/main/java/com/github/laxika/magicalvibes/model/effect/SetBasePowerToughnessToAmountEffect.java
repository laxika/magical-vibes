package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Min;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.amount.TargetPowerPlusToughness;
import com.github.laxika.magicalvibes.model.amount.TargetToughness;

/**
 * Continuous effect that sets the base power and toughness of creatures in a subject to
 * dynamically evaluated amounts (layer 7b, CR 613.4b). A floating instance may use {@link Subjects#source()} to
 * represent a granted self ability that changes only the source's base power or toughness.
 *
 * <p>Resolved as a one-shot effect, the amounts are determined once, as it resolves (CR 608.2h), and the values set
 * then last for {@code duration}: "target creature has base power and toughness X/X until end of turn" over
 * {@link Subjects#target()} (the amounts read the ability's source as it is now, else as it last existed), "this
 * creature's base power becomes equal to target creature's power" over {@link Subjects#source()} with a
 * {@code TargetPower} amount. As a static ability ("equal to …") the amounts are read continuously.
 *
 * <p>A {@code null} amount leaves that base value alone ("this creature's base toughness becomes …").
 *
 * <p>It declares a benign creature target when its subject is {@link Subjects#target()} or an amount reads the
 * target's power or toughness.
 *
 * @param power     the base power, or {@code null} to leave base power unchanged
 * @param toughness the base toughness, or {@code null} to leave base toughness unchanged
 * @param subject   the permanents whose base power and toughness are set
 * @param duration  how long a one-shot setter lasts; {@code null} for a target until end of turn, the source with no end
 */
public record SetBasePowerToughnessToAmountEffect(
        DynamicAmount power,
        DynamicAmount toughness,
        PermanentSubject subject,
        Duration duration
) implements CardEffect {

    /** For as long as the subject's form lasts: a target until end of turn, the source with no end. */
    public SetBasePowerToughnessToAmountEffect(DynamicAmount power, DynamicAmount toughness,
                                               PermanentSubject subject) {
        this(power, toughness, subject, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Target || readsTarget(power) || readsTarget(toughness)
                ? TargetSpec.benign(TargetPredicates.creature())
                : TargetSpec.NONE;
    }

    private static boolean readsTarget(DynamicAmount amount) {
        return switch (amount) {
            case null -> false;
            case TargetPower ignored -> true;
            case TargetToughness ignored -> true;
            case TargetPowerPlusToughness ignored -> true;
            case Sum sum -> sum.amounts().stream().anyMatch(SetBasePowerToughnessToAmountEffect::readsTarget);
            case Min min -> min.amounts().stream().anyMatch(SetBasePowerToughnessToAmountEffect::readsTarget);
            case Max max -> max.amounts().stream().anyMatch(SetBasePowerToughnessToAmountEffect::readsTarget);
            case Scaled scaled -> readsTarget(scaled.amount());
            default -> false;
        };
    }
}
