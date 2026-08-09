package com.github.laxika.magicalvibes.model.effect;

/** Redirects all damage that would be dealt to the target creature this turn to the effect's controller. */
public record RedirectAllDamageToTargetCreatureToControllerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
