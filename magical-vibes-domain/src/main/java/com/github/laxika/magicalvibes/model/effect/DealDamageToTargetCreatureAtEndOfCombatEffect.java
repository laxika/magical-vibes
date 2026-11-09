package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Schedules the source permanent to deal {@code damage} damage to the targeted creature at end of
 * combat. The delayed sibling of {@link DealDamageToTargetCreatureEffect}: the target is chosen
 * when the ability is activated, but the damage only happens once combat ends (Dwarven Sea Clan's
 * "{T}: Choose target attacking or blocking creature whose controller controls an Island. This
 * creature deals 2 damage to that creature at end of combat.").
 * <p>
 * Resolution queues a
 * {@link com.github.laxika.magicalvibes.model.action.DealDamageToPermanentAtEndOfCombat}, drained
 * in {@code CombatService.processEndOfCombatDamage()}. The source card is captured so the damage
 * still happens with last-known information if the source left the battlefield in the meantime.
 * <p>
 * Written with no declared target it also serves the Sawtooth Ogre wording "whenever this creature
 * blocks or becomes blocked by a creature, this creature deals N damage to that creature at end of
 * combat": on {@code Triggers.blocks()} the collector records the blocked attacker as the entry's
 * non-targeting target, and on {@code Triggers.becomesBlocked()} with {@code TriggerOptions.EACH_BLOCKER}
 * each blocker.
 *
 * @param damage the amount of damage the source deals at end of combat
 */
public record DealDamageToTargetCreatureAtEndOfCombatEffect(DynamicAmount damage)
        implements DamageDealingEffect {

    public DealDamageToTargetCreatureAtEndOfCombatEffect(int damage) {
        this(new Fixed(damage));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public DynamicAmount damageAmount() {
        return damage;
    }

    @Override
    public boolean canDamageCreatures() {
        return true;
    }

    @Override
    public boolean canDamagePlayers() {
        return false;
    }
}
