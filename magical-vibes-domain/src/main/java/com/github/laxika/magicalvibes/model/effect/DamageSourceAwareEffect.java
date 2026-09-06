package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Capability for triggered effects that need the damage source's permanent or card identity.
 */
public interface DamageSourceAwareEffect extends CardEffect {

    CardEffect bindDamageSource(Card sourceCard, UUID sourcePermanentId,
                                UUID sourceControllerId, int damageDealt);
}
