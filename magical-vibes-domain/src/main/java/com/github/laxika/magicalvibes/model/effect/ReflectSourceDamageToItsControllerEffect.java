package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.EventValue;

/**
 * Global-watcher trigger-condition record for {@code Triggers.sourceDealsDamage()}: whenever a source of the given
 * {@code color} (creature or spell) deals damage, the permanent holding this effect deals that much damage to that
 * source's controller (Justice). Never resolved directly: the collector puts {@link #triggeredEffect()} on the stack
 * with the watcher as the damage source, the summed damage as the entry's event value and the source (a permanent's
 * id, and its controller as it dealt the damage) as its triggering permanent.
 */
public record ReflectSourceDamageToItsControllerEffect(CardColor color) implements CardEffect {

    /**
     * "That much damage to that creature's or spell's controller", read from the stack entry as it resolves: a
     * creature's controller then, or as it last existed; a spell's controller as it dealt the damage.
     */
    public CardEffect triggeredEffect() {
        return new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER);
    }
}
