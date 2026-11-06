package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;

/**
 * Tephraderm's damage reflection: "this creature deals that much damage to that creature", or "to that spell's
 * controller". "That much" is the damage dealt, the stack entry's event value. The source is the entry's triggering
 * permanent when it was a permanent, whose being a creature is checked as the ability resolves; a source that was
 * never a permanent (a spell) has none, and its controller the entry recorded is dealt the damage.
 */
public record DealDamageToDamageSourceCreatureOrSpellControllerEffect() implements DamageDealingEffect {

    @Override
    public DynamicAmount damageAmount() {
        return new EventValue();
    }

    @Override
    public boolean canDamageCreatures() {
        return true;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }
}
