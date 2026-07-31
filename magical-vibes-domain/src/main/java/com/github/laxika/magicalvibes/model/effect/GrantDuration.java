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
    UNTIL_YOUR_NEXT_TURN,

    /**
     * "for as long as you control [source]" (Aegis Angel). Neither temporary bucket applies: the
     * grant is stamped as an {@code EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD} floating layer-6
     * effect keyed to the source permanent, so it survives cleanup and ends only when the source
     * leaves the battlefield ({@code GameData.expireFloatingEffectsForDepartedSource}) or its
     * creator loses control of it ({@code CreatureControlService}).
     */
    WHILE_SOURCE_ON_BATTLEFIELD,

    /**
     * No stated duration, so the grant lasts until the end of the game (CR 611.2a) — Nature's Blessing's
     * "that creature gains banding, first strike, or trample". Stamped as an
     * {@code EffectDuration.PERMANENT} floating layer-6 effect and mirrored into
     * {@code Permanent.persistentGrantedKeywords}, neither of which turn cleanup clears.
     */
    INDEFINITE
}
