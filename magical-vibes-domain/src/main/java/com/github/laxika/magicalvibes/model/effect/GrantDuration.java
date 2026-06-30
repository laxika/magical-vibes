package com.github.laxika.magicalvibes.model.effect;

/**
 * Duration of a <em>one-shot</em> (resolved) keyword grant.
 *
 * <p>This governs how long a keyword added by a resolving {@link GrantKeywordEffect} lingers on a
 * permanent. It maps directly onto the two temporary keyword buckets the engine already clears:
 * {@code Permanent.grantedKeywords} (cleared at the cleanup step) and
 * {@code Permanent.untilNextTurnKeywords} (cleared at the start of the controller's next turn).
 *
 * <p>The <em>continuous</em> / anthem case ("creatures you control have trample" for as long as the
 * source is on the battlefield) is <strong>not</strong> represented here — that is expressed by
 * placing the {@link GrantKeywordEffect} in {@code EffectSlot.STATIC}, where the static handler
 * recomputes it on the fly and ignores this enum.
 */
public enum GrantDuration {

    /** Lasts until end of turn; cleared by {@code TurnCleanupService} (via {@code Permanent.grantedKeywords}). */
    END_OF_TURN,

    /** Lasts until the start of the controller's next turn (via {@code Permanent.untilNextTurnKeywords}). */
    UNTIL_YOUR_NEXT_TURN
}
