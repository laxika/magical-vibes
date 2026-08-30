package com.github.laxika.magicalvibes.model.effect;

/**
 * "Creature spells you control can't be countered", optionally restricted to a minimum power.
 * Static ability on the source permanent.
 *
 * <p>With a non-null threshold, this protects only creature spells controlled by the source
 * permanent's controller whose power is at least that threshold (Spellbreaker Behemoth: 5).
 * With no threshold, it protects every creature spell controlled by that player (Rhythm of the
 * Wild).
 */
public record ControllerCreatureSpellsCantBeCounteredEffect(Integer minimumPower) implements CardEffect {

    public ControllerCreatureSpellsCantBeCounteredEffect() {
        this(null);
    }

    public ControllerCreatureSpellsCantBeCounteredEffect(int minimumPower) {
        this(Integer.valueOf(minimumPower));
    }
}
