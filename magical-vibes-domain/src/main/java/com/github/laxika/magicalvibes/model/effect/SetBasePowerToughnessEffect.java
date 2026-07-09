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
 * @param power     the base power to set
 * @param toughness the base toughness to set
 * @param scope     which permanents are affected ({@code TARGET} for the one-shot until-EOT usage,
 *                  {@code ENCHANTED_CREATURE}/{@code EQUIPPED_CREATURE}/etc. for continuous static)
 */
public record SetBasePowerToughnessEffect(int power, int toughness, GrantScope scope) implements CardEffect {

    /**
     * Convenience constructor for the one-shot "target creature has base power and toughness X/Y
     * until end of turn" usage.
     */
    public SetBasePowerToughnessEffect(int power, int toughness) {
        this(power, toughness, GrantScope.TARGET);
    }

    @Override
    public boolean canTargetPermanent() {
        return scope == GrantScope.TARGET;
    }
}
