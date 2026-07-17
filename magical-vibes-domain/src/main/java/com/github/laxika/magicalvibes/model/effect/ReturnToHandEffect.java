package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Returns permanent(s) to their owners' hands. Constructed only through the static factories below,
 * which are the safe, self-documenting way to pair a {@link BounceScope} with the parameters that
 * scope actually uses (a plain record can't hide its canonical constructor, so this is a final class
 * with a private constructor).
 *
 * <ul>
 *   <li>{@link #target()} / {@link #targetAndControllerLosesLife(int)} /
 *       {@link #targetAndControllerDraws(int)} — bounce the chosen target permanent(s); the life-loss
 *       variant makes each bounced permanent's controller lose life (Vapor Snag) and the draw variant
 *       makes each bounced permanent's controller draw (Call to Heel), the controller being
 *       snapshotted before the bounce.</li>
 *   <li>{@link #self()} — bounce the source permanent.</li>
 *   <li>{@link #allPermanentsMatching(PermanentPredicate)} — bounce every permanent matching the
 *       filter across all battlefields (null filter = every permanent).</li>
 *   <li>{@link #permanentsTargetPlayerControls(PermanentPredicate)} — bounce every permanent the
 *       target player controls matching the filter (River's Rebuke).</li>
 *   <li>{@link #permanentsTargetPlayerOwns(PermanentPredicate)} — bounce every permanent the target
 *       player owns matching the filter, regardless of controller (Hurkyl's Recall).</li>
 * </ul>
 */
public final class ReturnToHandEffect implements RemovalEffect, BoardWipeEffect {

    private final BounceScope scope;
    private final PermanentPredicate filter;
    private final int lifeLoss;
    private final int drawCount;

    private ReturnToHandEffect(BounceScope scope, PermanentPredicate filter, int lifeLoss, int drawCount) {
        this.scope = scope;
        this.filter = filter;
        this.lifeLoss = lifeLoss;
        this.drawCount = drawCount;
    }

    public static ReturnToHandEffect target() {
        return new ReturnToHandEffect(BounceScope.TARGET, null, 0, 0);
    }

    public static ReturnToHandEffect targetAndControllerLosesLife(int lifeLoss) {
        return new ReturnToHandEffect(BounceScope.TARGET, null, lifeLoss, 0);
    }

    public static ReturnToHandEffect targetAndControllerDraws(int drawCount) {
        return new ReturnToHandEffect(BounceScope.TARGET, null, 0, drawCount);
    }

    public static ReturnToHandEffect self() {
        return new ReturnToHandEffect(BounceScope.SELF, null, 0, 0);
    }

    /**
     * Returns the resolving spell card itself to its owner's hand instead of the graveyard
     * (instants/sorceries that bounce themselves off the stack — Redeem the Lost's won-clash reward).
     */
    public static ReturnToHandEffect selfSpell() {
        return new ReturnToHandEffect(BounceScope.SELF_SPELL, null, 0, 0);
    }

    public static ReturnToHandEffect allPermanentsMatching(PermanentPredicate filter) {
        return new ReturnToHandEffect(BounceScope.ALL_MATCHING, filter, 0, 0);
    }

    public static ReturnToHandEffect permanentsTargetPlayerControls(PermanentPredicate filter) {
        return new ReturnToHandEffect(BounceScope.TARGET_PLAYERS_PERMANENTS, filter, 0, 0);
    }

    public static ReturnToHandEffect permanentsTargetPlayerOwns(PermanentPredicate filter) {
        return new ReturnToHandEffect(BounceScope.TARGET_PLAYERS_OWNED, filter, 0, 0);
    }

    public BounceScope scope() {
        return scope;
    }

    public PermanentPredicate filter() {
        return filter;
    }

    public int lifeLoss() {
        return lifeLoss;
    }

    public int drawCount() {
        return drawCount;
    }

    @Override
    public TargetSpec targetSpec() {
        // Only the single-target scope targets a battlefield permanent (PERMANENT reproduces its
        // requireBattlefieldTarget guard); the target-players scopes target a player (the old
        // validator imposed no guard there); the self / all-matching scopes target nothing.
        if (scope == BounceScope.TARGET) {
            return TargetSpec.benign(TargetCategory.PERMANENT);
        }
        if (scope == BounceScope.TARGET_PLAYERS_PERMANENTS || scope == BounceScope.TARGET_PLAYERS_OWNED) {
            return TargetSpec.benign(TargetCategory.PLAYER);
        }
        return TargetSpec.NONE;
    }

    @Override
    public RemovalKind removalKind() {
        // Only a single-target bounce is targeted removal; the mass/self scopes are board
        // sweeps or self-return, not single-target removal.
        return scope == BounceScope.TARGET ? RemovalKind.BOUNCE : null;
    }

    @Override
    public boolean sweepsBoard() {
        // Only the all-matching scope is a board sweep; the targeted / self scopes are not.
        return scope == BounceScope.ALL_MATCHING;
    }
}
