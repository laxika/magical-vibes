package com.github.laxika.magicalvibes.model.effect;

/**
 * Sets the base power and toughness of the affected permanents to the given values.
 * Modifiers (counters, static boosts) still apply on top of the new base values (CR 613, layer 7b).
 *
 * <p>Two pipelines share this record, discriminated by {@code scope}:
 * <ul>
 *   <li>{@link GrantScope#TARGET} — one-shot, resolves off the stack and sets the targeted
 *       permanent's base P/T <em>until end of turn</em> (e.g. Diminish, Quandrix Charm).
 *       Handled by the normalfx {@code SetBasePowerToughnessEffectHandler}.</li>
 *   <li>{@link GrantScope#SELF} — one-shot, resolves off the stack and sets the <em>source's</em>
 *       base P/T until end of turn for a non-targeting ability ("this creature has base power and
 *       toughness X/Y until end of turn", e.g. Marsh Flitter). Also handled by the normalfx handler.</li>
 *   <li>Any other scope (e.g. {@link GrantScope#ENCHANTED_CREATURE}) — a continuous STATIC
 *       effect setting base P/T for as long as it applies (e.g. Deep Freeze, Darksteel Mutation).
 *       Handled by the staticfx {@code SetBasePowerToughnessStaticEffectHandler}.</li>
 * </ul>
 *
 * <p>A {@code null} component means "leave that base value alone" — "has base toughness 1"
 * (Chariot of the Sun) sets only the 7b toughness component and keeps the creature's printed
 * power. Partial setters are supported by both the one-shot and continuous static pipelines.
 *
 * @param power     the base power to set, or {@code null} to leave base power unchanged
 * @param toughness the base toughness to set, or {@code null} to leave base toughness unchanged
 * @param scope     which permanents are affected ({@code TARGET} for the one-shot until-EOT usage,
 *                  {@code ENCHANTED_CREATURE}/{@code EQUIPPED_CREATURE}/etc. for continuous static)
 * @param duration  how long a one-shot target or self setter remains active
 */
public record SetBasePowerToughnessEffect(Integer power, Integer toughness, GrantScope scope,
                                           EffectDuration duration) implements CardEffect {

    public SetBasePowerToughnessEffect(Integer power, Integer toughness, GrantScope scope) {
        this(power, toughness, scope, EffectDuration.UNTIL_END_OF_TURN);
    }

    /**
     * Convenience constructor for the one-shot "target creature has base power and toughness X/Y
     * until end of turn" usage.
     */
    public SetBasePowerToughnessEffect(int power, int toughness) {
        this(power, toughness, GrantScope.TARGET);
    }

    /**
     * One-shot "target creature has base toughness X until end of turn" — base power untouched.
     */
    public static SetBasePowerToughnessEffect toughnessOnly(int toughness) {
        return new SetBasePowerToughnessEffect(null, toughness, GrantScope.TARGET);
    }

    /**
     * One-shot "target creature has base power X until end of turn" — base toughness untouched.
     */
    public static SetBasePowerToughnessEffect powerOnly(int power) {
        return new SetBasePowerToughnessEffect(power, null, GrantScope.TARGET);
    }

    /** One-shot "target creature has base power and toughness 4/4" indefinitely. */
    public static SetBasePowerToughnessEffect indefinitely(int power, int toughness) {
        return new SetBasePowerToughnessEffect(power, toughness, GrantScope.TARGET,
                EffectDuration.PERMANENT);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (scope) {
            case TARGET -> TargetSpec.benign(TargetPredicates.creature());
            case TARGET_PLAYERS_CREATURES -> TargetSpec.benign(TargetPredicates.player());
            default -> TargetSpec.NONE;
        };
    }
}
