package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: if a red or artifact source controlled by this permanent's
 * controller would deal damage to an opponent or a permanent an opponent controls, it deals that
 * much damage plus {@code amount} instead.
 */
public record AdditionalDamageToOpponentsFromRedOrArtifactSourcesEffect(int amount) implements CardEffect {
}
