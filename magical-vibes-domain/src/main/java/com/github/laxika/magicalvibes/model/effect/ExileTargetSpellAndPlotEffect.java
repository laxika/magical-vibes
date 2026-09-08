package com.github.laxika.magicalvibes.model.effect;

/** Exiles the target spell and makes its card plotted. */
public record ExileTargetSpellAndPlotEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.spellOnStack());
    }
}
