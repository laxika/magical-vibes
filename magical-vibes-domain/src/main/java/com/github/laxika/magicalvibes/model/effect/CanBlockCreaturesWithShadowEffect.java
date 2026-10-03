package com.github.laxika.magicalvibes.model.effect;

/**
 * Static permission to block shadow creatures using either form of the "as though" wording.
 *
 * <p>The default treats the blocker as having shadow (Wall of Diffusion). The alternate form
 * treats the attacker as having no shadow (Aether Web), so a blocker that actually has shadow
 * still cannot block that attacker.</p>
 */
public record CanBlockCreaturesWithShadowEffect(boolean treatAttackerAsWithoutShadow)
        implements BlockabilityPermissionEffect {

    public CanBlockCreaturesWithShadowEffect() {
        this(false);
    }

    @Override
    public boolean blocksShadowAsThoughShadow() {
        return !treatAttackerAsWithoutShadow;
    }

    @Override
    public boolean blocksShadowAsThoughNoShadow() {
        return treatAttackerAsWithoutShadow;
    }
}
