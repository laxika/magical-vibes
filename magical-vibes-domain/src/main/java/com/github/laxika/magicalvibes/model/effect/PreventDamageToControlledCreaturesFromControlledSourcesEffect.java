package com.github.laxika.magicalvibes.model.effect;

/** Static effect that prevents damage from sources you control to creatures you control. */
public record PreventDamageToControlledCreaturesFromControlledSourcesEffect()
        implements ControlledSourceCreatureDamagePreventionEffect {
}
