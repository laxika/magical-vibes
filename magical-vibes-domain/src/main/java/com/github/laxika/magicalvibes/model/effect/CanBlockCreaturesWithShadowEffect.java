package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: this creature can block creatures with shadow as though it had shadow.
 *
 * <p>Only lifts the shadow blocking restriction in the "attacker has shadow, blocker doesn't"
 * direction — the carrier does not gain shadow, so it still blocks creatures without shadow
 * normally and is still blockable as a non-shadow creature (Wall of Diffusion).</p>
 */
public record CanBlockCreaturesWithShadowEffect() implements BlockabilityPermissionEffect {

    @Override
    public boolean blocksShadowAsThoughShadow() {
        return true;
    }
}
