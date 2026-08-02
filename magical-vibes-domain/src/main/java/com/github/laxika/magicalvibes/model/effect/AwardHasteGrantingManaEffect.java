package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;

/**
 * Produces unrestricted mana that carries a rider: "If that mana is spent on a creature spell, it
 * gains haste until end of turn" (Generator Servant). The mana lands in the regular pool and can be
 * spent on anything; the rider is a tag on that subset of the pool
 * ({@link ManaPool#addHasteGrantingMana}) which the spell-casting path reads after payment.
 *
 * @param color  the color of mana produced
 * @param amount how much mana is produced
 */
public record AwardHasteGrantingManaEffect(ManaColor color, int amount) implements ManaProducingEffect {

    public void applyTo(ManaPool pool) {
        pool.add(color, amount);
        pool.addHasteGrantingMana(color, amount);
    }
}
