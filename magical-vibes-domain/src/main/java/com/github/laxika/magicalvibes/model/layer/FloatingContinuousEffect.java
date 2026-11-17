package com.github.laxika.magicalvibes.model.layer;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CounterConditionedControlEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetHauntedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAuraAttachedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MonarchBoundControlEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.UUID;

/**
 * A continuous effect created by a resolved spell or ability (CR 611.2) that lives on
 * {@code GameData} rather than being derived from a static ability of a permanent — e.g.
 * Giant Growth's +3/+3, Threaten's control change, Merfolk Trickster's "loses all abilities".
 * Introduced by the CR 613 layer-system migration (see {@code agent-docs/LAYER_SYSTEM.md});
 * dumb data holder, all interpretation happens in the layered pass.
 *
 * <p>At most one of {@link #affectedPermanentId}, {@link #affectedPlayerId}, {@link #affectedCardId} or
 * {@link #scope} identifies what the effect applies to; an effect that names none applies to every player.
 * {@link #sourcePermanentId} is {@code null} when the source was a spell (it never had a permanent) or has
 * since left the battlefield.
 *
 * @param id                  unique identity of this floating effect
 * @param sourceCardName      name of the card whose spell/ability created the effect
 * @param sourcePermanentId   the source permanent, or {@code null} (spells, departed sources)
 * @param controllerId        controller of the spell/ability that created the effect
 * @param effect              the wrapped effect describing what the continuous effect does
 * @param affectedPermanentId the single permanent affected, or {@code null}
 * @param affectedPlayerId    the single player affected, or {@code null}
 * @param scope               predicate selecting the affected permanents, or {@code null}
 * @param duration            how long the effect lasts (see {@link Duration})
 * @param timestamp           CR 613.7 timestamp, taken when the creating spell/ability resolved
 * @param expiresWhenScopeNoLongerMatches whether a permanent stops being affected once it stops matching
 *                            {@link #scope}
 * @param affectedCardId      the single card affected outside the battlefield (a card in exile, a graveyard or a
 *                            hand), or {@code null}; the effect ends as that card leaves its zone, since the card
 *                            then becomes a new object (CR 400.7)
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
        Duration duration,
        long timestamp,
        boolean expiresWhenScopeNoLongerMatches,
        UUID affectedCardId) {

    public FloatingContinuousEffect(UUID id, String sourceCardName, UUID sourcePermanentId, UUID controllerId,
                                    CardEffect effect, UUID affectedPermanentId, UUID affectedPlayerId,
                                    PermanentPredicate scope, Duration duration, long timestamp,
                                    boolean expiresWhenScopeNoLongerMatches) {
        this(id, sourceCardName, sourcePermanentId, controllerId, effect, affectedPermanentId, affectedPlayerId,
                scope, duration, timestamp, expiresWhenScopeNoLongerMatches, null);
    }

    public FloatingContinuousEffect(UUID id, String sourceCardName, UUID sourcePermanentId, UUID controllerId,
                                    CardEffect effect, UUID affectedPermanentId, UUID affectedPlayerId,
                                    PermanentPredicate scope, Duration duration, long timestamp) {
        this(id, sourceCardName, sourcePermanentId, controllerId, effect, affectedPermanentId,
                affectedPlayerId, scope, duration, timestamp, false);
    }

    /**
     * A rules-modifying effect on {@code playerId} (CR 611.2a), or on every player when {@code playerId} is
     * {@code null}: "you can't lose life this turn", "players can't gain life this turn". Its timestamp is stamped as
     * it is added.
     */
    public static FloatingContinuousEffect onPlayer(String sourceCardName, UUID sourcePermanentId, UUID controllerId,
                                                    CardEffect effect, UUID playerId, Duration duration) {
        return new FloatingContinuousEffect(UUID.randomUUID(), sourceCardName, sourcePermanentId, controllerId, effect,
                null, playerId, null, duration, 0);
    }

    /**
     * A rules-modifying effect on the permanent {@code permanentId} (CR 611.2a): "it can't block this turn", "prevent
     * all damage that would be dealt to it this turn". Its timestamp is stamped as it is added.
     */
    public static FloatingContinuousEffect onPermanent(String sourceCardName, UUID sourcePermanentId,
                                                       UUID controllerId, CardEffect effect, UUID permanentId,
                                                       Duration duration) {
        return new FloatingContinuousEffect(UUID.randomUUID(), sourceCardName, sourcePermanentId, controllerId, effect,
                permanentId, null, null, duration, 0);
    }

    /**
     * An effect on the card {@code cardId} in a zone other than the battlefield (CR 611.2a): "you may play that card
     * this turn", "it gains flashback until end of turn". It ends as the card leaves that zone (CR 400.7). Its
     * timestamp is stamped as it is added.
     */
    public static FloatingContinuousEffect onCard(String sourceCardName, UUID sourcePermanentId, UUID controllerId,
                                                  CardEffect effect, UUID cardId, Duration duration) {
        return new FloatingContinuousEffect(UUID.randomUUID(), sourceCardName, sourcePermanentId, controllerId, effect,
                null, null, null, duration, 0, false, cardId);
    }

    /** Whether this effect applies to {@code playerId}: it names that player, or it names no object at all. */
    public boolean appliesToPlayer(UUID playerId) {
        return affectedPlayerId == null ? affectedPermanentId == null && scope == null && affectedCardId == null
                : affectedPlayerId.equals(playerId);
    }

    /** Whether this is a CR 613 layer-2 control-changing effect. All floating control effects
     *  wrap one of these effect types (see {@code CreatureControlService}). */
    public boolean isControlEffect() {
        return effect instanceof CounterConditionedControlEffect
                || effect instanceof GainControlOfTargetEffect
                || effect instanceof GainControlOfTargetHauntedCreatureEffect
                || effect instanceof ControlEnchantedCreatureEffect
                || effect instanceof GainControlOfEnchantedTargetEffect
                || effect instanceof GainControlOfEnchantedPermanentEffect
                || effect instanceof GainControlOfAuraAttachedPermanentEffect
                || effect instanceof MonarchBoundControlEffect;
    }

    /** Returns a copy of this effect carrying the given CR 613.7 timestamp (used by
     *  {@code GameData.addFloatingEffect} to stamp the effect on insertion). */
    /** This effect with another payload, keeping its identity, place and timestamp. */
    public FloatingContinuousEffect withEffect(CardEffect effect) {
        return new FloatingContinuousEffect(id, sourceCardName, sourcePermanentId, controllerId,
                effect, affectedPermanentId, affectedPlayerId, scope, duration, timestamp, expiresWhenScopeNoLongerMatches,
                affectedCardId);
    }

    public FloatingContinuousEffect withTimestamp(long timestamp) {
        return new FloatingContinuousEffect(id, sourceCardName, sourcePermanentId, controllerId,
                effect, affectedPermanentId, affectedPlayerId, scope, duration, timestamp, expiresWhenScopeNoLongerMatches,
                affectedCardId);
    }
}
