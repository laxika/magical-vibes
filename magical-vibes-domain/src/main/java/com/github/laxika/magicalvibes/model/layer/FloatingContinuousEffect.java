package com.github.laxika.magicalvibes.model.layer;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.UUID;

/**
 * A continuous effect created by a resolved spell or ability (CR 611.2) that lives on
 * {@code GameData} rather than being derived from a static ability of a permanent — e.g.
 * Giant Growth's +3/+3, Threaten's control change, Merfolk Trickster's "loses all abilities".
 * Introduced by the CR 613 layer-system migration (see {@code agent-docs/LAYER_SYSTEM.md});
 * dumb data holder, all interpretation happens in the layered pass.
 *
 * <p>Exactly one of {@link #affectedPermanentId}, {@link #affectedPlayerId}, or {@link #scope}
 * identifies what the effect applies to. {@link #sourcePermanentId} is {@code null} when the
 * source was a spell (it never had a permanent) or has since left the battlefield.
 *
 * @param id                  unique identity of this floating effect
 * @param sourceCardName      name of the card whose spell/ability created the effect
 * @param sourcePermanentId   the source permanent, or {@code null} (spells, departed sources)
 * @param controllerId        controller of the spell/ability that created the effect
 * @param effect              the wrapped effect describing what the continuous effect does
 * @param affectedPermanentId the single permanent affected, or {@code null}
 * @param affectedPlayerId    the single player affected, or {@code null}
 * @param scope               predicate selecting the affected permanents, or {@code null}
 * @param duration            how long the effect lasts (see {@link EffectDuration})
 * @param timestamp           CR 613.7 timestamp, taken when the creating spell/ability resolved
 */
public record FloatingContinuousEffect(
        UUID id,
        String sourceCardName,
        UUID sourcePermanentId,
        UUID controllerId,
        CardEffect effect,
        UUID affectedPermanentId,
        UUID affectedPlayerId,
        PermanentPredicate scope,
        EffectDuration duration,
        long timestamp) {

    /** Returns a copy of this effect carrying the given CR 613.7 timestamp (used by
     *  {@code GameData.addFloatingEffect} to stamp the effect on insertion). */
    public FloatingContinuousEffect withTimestamp(long timestamp) {
        return new FloatingContinuousEffect(id, sourceCardName, sourcePermanentId, controllerId,
                effect, affectedPermanentId, affectedPlayerId, scope, duration, timestamp);
    }
}
