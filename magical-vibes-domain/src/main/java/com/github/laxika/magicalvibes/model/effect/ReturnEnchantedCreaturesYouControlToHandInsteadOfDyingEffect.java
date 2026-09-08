package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement that returns an enchanted creature controlled by the source's controller to
 * its owner's hand instead of letting it die.
 */
public record ReturnEnchantedCreaturesYouControlToHandInsteadOfDyingEffect()
        implements DyingCreatureReturnToHandReplacementEffect {
}
