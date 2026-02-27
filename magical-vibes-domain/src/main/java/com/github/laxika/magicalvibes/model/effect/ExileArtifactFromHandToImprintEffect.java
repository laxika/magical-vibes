package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles an artifact card from the controller's hand and imprints it on the source permanent.
 * Used by Prototype Portal's ETB trigger.
 */
public record ExileArtifactFromHandToImprintEffect() implements CardEffect {
}
