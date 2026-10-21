package com.github.laxika.magicalvibes.model.effect;

/**
 * Resolves Tibalt's Trickery's random mill and library-exile rider for its target spell.
 *
 * <p>The effect follows the counter effect, as the Oracle text orders them. By then the target spell
 * has left the stack, so the rider reads its controller and name from the resolving entry's
 * last-known information (an uncounterable spell is still on the stack and is read directly).
 */
public record TibaltTrickeryEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
