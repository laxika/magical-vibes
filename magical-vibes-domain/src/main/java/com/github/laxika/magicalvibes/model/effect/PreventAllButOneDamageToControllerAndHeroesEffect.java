package com.github.laxika.magicalvibes.model.effect;

/** Static effect that reduces damage to its controller or their Heroes to at most one. */
public record PreventAllButOneDamageToControllerAndHeroesEffect()
        implements AllButOneDamagePreventionEffect {

    @Override
    public boolean protectsHeroes() {
        return true;
    }
}
