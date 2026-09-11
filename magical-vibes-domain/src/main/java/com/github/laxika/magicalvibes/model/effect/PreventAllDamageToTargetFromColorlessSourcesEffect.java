package com.github.laxika.magicalvibes.model.effect;

/** Targeted spell effect that prevents all damage from colorless sources to the target this turn. */
public record PreventAllDamageToTargetFromColorlessSourcesEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
