package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller of the targeted spell looks at their hand.
 *
 * <p>This companion effect does not independently target a player. It reads the spell from the
 * stack entry's target and must resolve before an accompanying counter effect.
 */
public record TargetSpellControllerLooksAtHandEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
