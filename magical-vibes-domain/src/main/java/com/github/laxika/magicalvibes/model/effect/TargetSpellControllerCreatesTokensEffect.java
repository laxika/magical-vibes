package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates the supplied tokens under the controller of the spell targeted by this stack entry.
 * The targeted spell must still be on the stack when this resolves, so cards should place this
 * effect before any accompanying counter effect.
 */
public record TargetSpellControllerCreatesTokensEffect(CreateTokenEffect tokenEffect) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
