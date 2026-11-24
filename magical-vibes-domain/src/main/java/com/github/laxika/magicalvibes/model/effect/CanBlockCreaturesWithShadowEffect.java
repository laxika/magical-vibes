package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static permission for the carrier (the creature, or the creature an Aura carrying it enchants) to block through an
 * evasion ability "as though" something were different. It changes no characteristics: the carrier gains no shadow or
 * reach and the attacker loses no ability, so only the one blocking restriction is lifted.
 *
 * <p>Modes: {@link Mode#AS_THOUGH_SHADOW} "can block creatures with shadow as though it had shadow" (the no-argument
 * form; Wall of Diffusion); {@link Mode#IGNORING_SHADOW} "as though they didn't have shadow" ({@code (true)}; Aether
 * Web), so a blocker that has shadow still can't block that attacker; {@link Mode#IGNORING_LANDWALK} "can block
 * creatures with landwalk abilities as though they didn't have those abilities" (Street Savvy);
 * {@link Mode#AS_THOUGH_REACH} "can block [attackers] as though it had reach" over the matching attackers (Dragon
 * Hunter).
 *
 * @param mode      which restriction the carrier blocks through
 * @param attackers for {@link Mode#AS_THOUGH_REACH}, the attackers it can block as though it had reach; {@code null}
 *                  for the other modes
 */
public record CanBlockCreaturesWithShadowEffect(Mode mode, PermanentPredicate attackers)
        implements BlockabilityPermissionEffect {

    /** Which evasion ability the carrier blocks through, and how. */
    public enum Mode {
        AS_THOUGH_SHADOW, IGNORING_SHADOW, IGNORING_LANDWALK, AS_THOUGH_REACH
    }

    public CanBlockCreaturesWithShadowEffect {
        if ((mode == Mode.AS_THOUGH_REACH) != (attackers != null)) {
            throw new IllegalArgumentException("Only the as-though-reach mode takes an attacker filter: " + mode);
        }
    }

    /** "Can block creatures with shadow as though [this creature] had shadow." */
    public CanBlockCreaturesWithShadowEffect() {
        this(false);
    }

    /**
     * Shadow permission: {@code treatAttackerAsWithoutShadow} is "as though they didn't have shadow", else "as though
     * [this creature] had shadow".
     */
    public CanBlockCreaturesWithShadowEffect(boolean treatAttackerAsWithoutShadow) {
        this(treatAttackerAsWithoutShadow ? Mode.IGNORING_SHADOW : Mode.AS_THOUGH_SHADOW, null);
    }

    /** "Can block creatures with landwalk abilities as though they didn't have those abilities." */
    public static CanBlockCreaturesWithShadowEffect ignoringLandwalk() {
        return new CanBlockCreaturesWithShadowEffect(Mode.IGNORING_LANDWALK, null);
    }

    /** "Can block [subtype]s as though it had reach." */
    public static CanBlockCreaturesWithShadowEffect asThoughReachAgainst(CardSubtype subtype) {
        return new CanBlockCreaturesWithShadowEffect(Mode.AS_THOUGH_REACH, new PermanentHasSubtypePredicate(subtype));
    }

    @Override
    public boolean blocksShadowAsThoughShadow() {
        return mode == Mode.AS_THOUGH_SHADOW;
    }

    @Override
    public boolean blocksShadowAsThoughNoShadow() {
        return mode == Mode.IGNORING_SHADOW;
    }

    @Override
    public boolean blocksLandwalkAsThoughNoLandwalk() {
        return mode == Mode.IGNORING_LANDWALK;
    }

    @Override
    public PermanentPredicate blocksAsThoughReachForAttackers() {
        return attackers;
    }
}
