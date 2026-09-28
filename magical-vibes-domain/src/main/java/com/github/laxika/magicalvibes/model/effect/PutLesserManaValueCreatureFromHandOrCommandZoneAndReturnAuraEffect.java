package com.github.laxika.magicalvibes.model.effect;

/**
 * Next of Kin's death trigger: optionally put a lower-mana-value creature from hand or the
 * command zone onto the battlefield, then return the source Aura attached to it at the next end
 * step.
 */
public record PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect(
        Integer dyingCreatureManaValue
) implements CardEffect, DyingCreatureManaValueAwareEffect {

    /** Card-definition constructor; the trigger collector binds the dying creature's mana value. */
    public PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect() {
        this(null);
    }

    @Override
    public CardEffect snapshotDyingCreatureManaValue(int manaValue) {
        return new PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect(manaValue);
    }
}
