package com.github.laxika.magicalvibes.model.effect;

/** On resolution, choose a color and prevent all damage from that color to the target this turn. */
public record PreventAllDamageToTargetFromChosenColorEffect(boolean creatureOnly) implements CardEffect {

    public PreventAllDamageToTargetFromChosenColorEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(creatureOnly ? TargetPredicates.creature() : TargetPredicates.anyTarget());
    }
}
