package com.github.laxika.magicalvibes.model.effect;

/**
 * Winter's Chill: for each targeted attacking creature, its controller may pay {1} or {2}.
 * Pay nothing → destroy that creature at end of combat. Pay only {1} → prevent all combat damage
 * dealt to and by that creature this combat. Pay {2} → no further effect on that creature.
 *
 * <p>Resolved by {@code WintersChillEffectHandler} one target at a time via a three-way list pick.
 * Bound to an X-scaled attacking-creature target group ({@code targetX(...)}).
 */
public record WintersChillEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.CREATURE);
    }
}
