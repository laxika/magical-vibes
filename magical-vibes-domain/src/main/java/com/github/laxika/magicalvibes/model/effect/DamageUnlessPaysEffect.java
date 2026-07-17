package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Punisher effect: the source permanent deals {@code damage} damage to the triggering player
 * unless they pay {@code payAmount} generic. Soul Barrier (2 damage, {2}, creature spells only).
 * The affected player chooses whether to pay or take the damage; can't-pay auto-applies it.
 *
 * <p>Distinct from {@link LoseLifeUnlessPaysEffect}: the penalty is routed through the normal
 * damage system (prevention shields, redirection, protection, infect all apply), whereas life
 * loss cannot be prevented.
 *
 * @param damage      how much damage the player takes if they don't pay
 * @param payAmount   generic mana cost the player can pay to avoid the damage
 * @param spellFilter optional filter for which spells trigger this (null = any spell)
 */
public record DamageUnlessPaysEffect(int damage, int payAmount, CardPredicate spellFilter)
        implements DamageDealingEffect {

    public DamageUnlessPaysEffect(int damage, int payAmount) {
        this(damage, payAmount, null);
    }

    @Override
    public DynamicAmount damageAmount() {
        return new Fixed(damage);
    }

    @Override
    public boolean canDamageCreatures() {
        return false;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }
}
