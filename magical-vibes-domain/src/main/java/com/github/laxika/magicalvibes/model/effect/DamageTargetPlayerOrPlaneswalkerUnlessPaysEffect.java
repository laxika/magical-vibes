package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The pushed half of Quenchable Fire's delayed trigger. When it resolves at the spell controller's
 * next upkeep, the affected party ({@code entry.controllerId}, the paying player) may pay
 * {@code manaCost}; if they don't (or can't), {@code damage} is dealt to {@code entry.targetId} — the
 * originally-targeted player or planeswalker — through the normal damage system.
 *
 * <p>Never a directly-castable effect: it is only ever placed on the stack by
 * {@code StepTriggerService} when draining a {@code DamageAtNextUpkeepUnlessPays} delayed action, with
 * both the payer (controllerId) and the damage recipient (targetId) already resolved. Declares no
 * target of its own ({@link TargetSpec#NONE}).
 *
 * @param damage   damage dealt to the target if the affected party doesn't pay
 * @param manaCost mana the affected party may pay to avoid the damage (e.g. {@code "{U}"})
 */
public record DamageTargetPlayerOrPlaneswalkerUnlessPaysEffect(int damage, String manaCost)
        implements CardEffect, DamageDealingEffect {

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
