package com.github.laxika.magicalvibes.model.effect;

/** Exiles every spell chosen for this effect's target group. */
public record ExileEachTargetSpellEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.spellOnStack());
    }
}
