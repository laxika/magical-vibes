package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.UUID;

/**
 * Tephraderm's damage reflection trigger. The card-level marker is bound to either the creature
 * that dealt the damage or the controller of a spell that dealt the damage when the trigger fires.
 */
public record DealDamageToDamageSourceCreatureOrSpellControllerEffect(
        int amount, UUID damageSourceCreatureId, UUID spellControllerId)
        implements DamageDealingEffect, DamageSourceAwareEffect {

    /** Marker constructor used on the card definition. */
    public DealDamageToDamageSourceCreatureOrSpellControllerEffect() {
        this(0, null, null);
    }

    @Override
    public CardEffect bindDamageSource(Card sourceCard, UUID sourcePermanentId,
                                       UUID sourceControllerId, int damageDealt) {
        if (sourceCard == null || damageDealt <= 0) return this;
        // Any permanent source binds as the creature: whether it is a creature (an animated land
        // is one without being a creature card) is checked when the trigger resolves.
        if (sourcePermanentId != null) {
            return new DealDamageToDamageSourceCreatureOrSpellControllerEffect(
                    damageDealt, sourcePermanentId, null);
        }
        if (sourcePermanentId == null && sourceControllerId != null) {
            return new DealDamageToDamageSourceCreatureOrSpellControllerEffect(
                    damageDealt, null, sourceControllerId);
        }
        return this;
    }

    @Override
    public DynamicAmount damageAmount() {
        return new Fixed(amount);
    }

    @Override
    public boolean canDamageCreatures() {
        return damageSourceCreatureId != null;
    }

    @Override
    public boolean canDamagePlayers() {
        return spellControllerId != null;
    }
}
