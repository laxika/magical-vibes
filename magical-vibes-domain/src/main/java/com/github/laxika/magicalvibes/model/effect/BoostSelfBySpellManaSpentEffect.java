package com.github.laxika.magicalvibes.model.effect;

/**
 * Boosts the source permanent by a multiple of X until end of turn, where X is the amount of mana
 * spent to cast the spell that triggered this effect.
 * <p>
 * Used as a resolved effect inside {@link SpellCastTriggerEffect} on the
 * {@code ON_CONTROLLER_CASTS_SPELL} slot. The trigger collector sets {@code xValue} on the
 * stack entry from the casting spell's mana payment.
 */
public record BoostSelfBySpellManaSpentEffect(
        int powerMultiplier,
        int toughnessMultiplier
) implements CardEffect {

    @Override
    public boolean isSelfTargeting() {
        return true;
    }
}
