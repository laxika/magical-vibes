package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.UUID;

/**
 * Sealed hierarchy of trigger event contexts.
 * Each record carries the event-specific data that trigger collectors may need.
 */
public sealed interface TriggerContext {

    /**
     * Context for spell-cast triggers (ON_ANY_PLAYER_CASTS_SPELL, ON_CONTROLLER_CASTS_SPELL, ON_OPPONENT_CASTS_SPELL).
     */
    record SpellCast(Card spellCard, UUID castingPlayerId, boolean castFromHand) implements TriggerContext {}

    /**
     * Context for discard triggers (ON_OPPONENT_DISCARDS).
     */
    record Discard(UUID discardingPlayerId, Card discardedCard) implements TriggerContext {}

    /**
     * Context for land-tap triggers (ON_ANY_PLAYER_TAPS_LAND).
     */
    record LandTap(UUID tappingPlayerId, UUID tappedLandId) implements TriggerContext {}

    /**
     * Context for damage-dealt-to-controller triggers (ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU).
     */
    record DamageToController(UUID damagedPlayerId, UUID sourcePermanentId, boolean isCombatDamage) implements TriggerContext {}

    /**
     * Context for ally-permanent-sacrificed triggers (ON_ALLY_PERMANENT_SACRIFICED).
     */
    record AllySacrificed(UUID sacrificingPlayerId) implements TriggerContext {}

    /**
     * Context for dealt-damage-to-creature triggers (ON_DEALT_DAMAGE).
     */
    record DamageToCreature(Permanent damagedCreature, int damageDealt, UUID damageSourceControllerId) implements TriggerContext {}

    /**
     * Context for enchanted-permanent-tap triggers (ON_ENCHANTED_PERMANENT_TAPPED).
     */
    record EnchantedPermanentTap(Permanent tappedPermanent, UUID tappedPermanentControllerId) implements TriggerContext {}

    /**
     * Context for life-loss triggers (ON_OPPONENT_LOSES_LIFE).
     */
    record LifeLoss(UUID losingPlayerId, int lifeLostAmount) implements TriggerContext {}
}
