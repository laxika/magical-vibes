package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Returns permanent(s) to their owners' hands. Constructed only through the static factories below,
 * which are the safe, self-documenting way to pair a {@link BounceScope} with the parameters that
 * scope actually uses (a plain record can't hide its canonical constructor, so this is a final class
 * with a private constructor).
 *
 * <ul>
 *   <li>{@link #target()} / {@link #targetAndControllerLosesLife(int)} — bounce the chosen target
 *       permanent(s); the life-loss variant makes each bounced permanent's controller lose life
 *       (Vapor Snag), the controller being snapshotted before the bounce.</li>
 *   <li>{@link #self()} — bounce the source permanent.</li>
 *   <li>{@link #allPermanentsMatching(PermanentPredicate)} — bounce every permanent matching the
 *       filter across all battlefields (null filter = every permanent).</li>
 *   <li>{@link #permanentsTargetPlayerControls(PermanentPredicate)} — bounce every permanent the
 *       target player controls matching the filter (River's Rebuke).</li>
 *   <li>{@link #permanentsTargetPlayerOwns(PermanentPredicate)} — bounce every permanent the target
 *       player owns matching the filter, regardless of controller (Hurkyl's Recall).</li>
 * </ul>
 */
public final class ReturnToHandEffect implements CardEffect {

    private final BounceScope scope;
    private final PermanentPredicate filter;
    private final int lifeLoss;

    private ReturnToHandEffect(BounceScope scope, PermanentPredicate filter, int lifeLoss) {
        this.scope = scope;
        this.filter = filter;
        this.lifeLoss = lifeLoss;
    }

    public static ReturnToHandEffect target() {
        return new ReturnToHandEffect(BounceScope.TARGET, null, 0);
    }

    public static ReturnToHandEffect targetAndControllerLosesLife(int lifeLoss) {
        return new ReturnToHandEffect(BounceScope.TARGET, null, lifeLoss);
    }

    public static ReturnToHandEffect self() {
        return new ReturnToHandEffect(BounceScope.SELF, null, 0);
    }

    public static ReturnToHandEffect allPermanentsMatching(PermanentPredicate filter) {
        return new ReturnToHandEffect(BounceScope.ALL_MATCHING, filter, 0);
    }

    public static ReturnToHandEffect permanentsTargetPlayerControls(PermanentPredicate filter) {
        return new ReturnToHandEffect(BounceScope.TARGET_PLAYERS_PERMANENTS, filter, 0);
    }

    public static ReturnToHandEffect permanentsTargetPlayerOwns(PermanentPredicate filter) {
        return new ReturnToHandEffect(BounceScope.TARGET_PLAYERS_OWNED, filter, 0);
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

    @Override
    public boolean canTargetPermanent() {
        return scope == BounceScope.TARGET;
    }

    @Override
    public boolean canTargetPlayer() {
        return scope == BounceScope.TARGET_PLAYERS_PERMANENTS || scope == BounceScope.TARGET_PLAYERS_OWNED;
    }
}
