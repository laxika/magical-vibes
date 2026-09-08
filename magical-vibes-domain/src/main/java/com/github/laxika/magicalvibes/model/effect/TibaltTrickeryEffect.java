package com.github.laxika.magicalvibes.model.effect;

/**
 * Resolves Tibalt's Trickery's random mill and library-exile rider for its target spell.
 *
 * <p>The effect is placed before the counter effect so the target spell's controller and name are
 * available while the rider resolves. The counter effect follows it and uses the same spell target.
 */
public record TibaltTrickeryEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
