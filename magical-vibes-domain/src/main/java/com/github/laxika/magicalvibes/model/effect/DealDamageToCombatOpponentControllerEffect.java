package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Combat trigger: this permanent deals {@code amount} damage to the controller of its combat
 * opponent — the creature it blocks (or that becomes blocked by it). Meglonoth's "Whenever this
 * creature blocks a creature, this creature deals damage to that creature's controller equal to
 * this creature's power" ({@code amount = new SourcePower()}).
 * <p>
 * Placed on the {@code Triggers.blocks()} ability (for the
 * "blocks" half). The collector records the creature on the other side of the block (on an Aura or
 * Equipment, the enchanted or equipped creature's) as the stack entry's non-targeting target, so
 * the ability can't fizzle. At resolution the source ({@code sourcePermanentId}, so {@code amount} such as
 * {@link com.github.laxika.magicalvibes.model.amount.SourcePower} reads this permanent) deals the
 * damage to the opponent's controller.
 */
public record DealDamageToCombatOpponentControllerEffect(DynamicAmount amount)
        implements CardEffect, DamageDealingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }

    @Override
    public DynamicAmount damageAmount() {
        return amount;
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
