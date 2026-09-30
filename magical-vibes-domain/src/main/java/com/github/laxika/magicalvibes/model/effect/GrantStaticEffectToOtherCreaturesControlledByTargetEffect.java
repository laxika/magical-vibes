package com.github.laxika.magicalvibes.model.effect;

/**
 * Permanently grants a static effect to each other creature controlled by the targeted creature's
 * controller. The affected creatures are determined when this effect resolves.
 *
 * @param staticEffect the static effect each affected creature gains
 */
public record GrantStaticEffectToOtherCreaturesControlledByTargetEffect(CardEffect staticEffect)
        implements CardEffect {

    /**
     * The card's creature target is declared by a sibling effect; this effect derives the target's
     * controller from the resolving stack entry.
     */
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
