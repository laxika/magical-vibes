package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Combat trigger: this permanent deals {@code amount} damage to the controller of its combat
 * opponent — the creature it blocks (or that becomes blocked by it). Meglonoth's "Whenever this
 * creature blocks a creature, this creature deals damage to that creature's controller equal to
 * this creature's power" ({@code amount = new SourcePower()}).
 * <p>
 * Placed on the {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_BLOCK} slot (for the
 * "blocks" half, auto-targeting the blocked attacker). As a {@link CombatOpponentReferencingEffect}
 * the combat opponent is passed as the stack entry's non-targeting target (it can't fizzle) and, on
 * an aura/equipment, {@code CombatTriggerService} auto-targets the enchanted/equipped creature's
 * combat opponent. At resolution the source ({@code sourcePermanentId}, so {@code amount} such as
 * {@link com.github.laxika.magicalvibes.model.amount.SourcePower} reads this permanent) deals the
 * damage to the opponent's controller.
 */
public record DealDamageToCombatOpponentControllerEffect(DynamicAmount amount)
        implements CardEffect, CombatOpponentReferencingEffect, DamageDealingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PERMANENT);
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
