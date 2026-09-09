package com.github.laxika.magicalvibes.model.effect;

/** Redirects all damage that would be dealt to the source creature this turn to target creature instead. */
public record RedirectAllDamageFromSourceCreatureToTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
