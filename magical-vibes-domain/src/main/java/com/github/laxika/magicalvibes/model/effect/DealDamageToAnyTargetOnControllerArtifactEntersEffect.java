package com.github.laxika.magicalvibes.model.effect;

/**
 * Emblem marker for "Whenever an artifact you control enters, this emblem deals damage to any
 * target." The trigger collector creates the targeted damage ability when the artifact enters.
 */
public record DealDamageToAnyTargetOnControllerArtifactEntersEffect(int damage)
        implements EmblemArtifactEntersTriggerEffect {
}
