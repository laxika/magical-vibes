package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;

/**
 * Produces unrestricted mana that carries a rider: "If that mana is spent on an instant or sorcery
 * spell, that spell can't be countered" (Boseiju, Who Shelters All). The mana lands in the regular
 * pool and can be spent on anything; the rider is a tag on that subset of the pool
 * ({@link ManaPool#addUncounterableGrantingMana}) which the spell-casting path reads after payment
 * and, for an instant or sorcery, records in {@code GameData.spellsMadeUncounterable}.
 *
 * @param color  the color of mana produced
 * @param amount how much mana is produced
 */
public record AwardUncounterableGrantingManaEffect(ManaColor color, int amount) implements ManaProducingEffect {

    public void applyTo(ManaPool pool) {
        pool.add(color, amount);
        pool.addUncounterableGrantingMana(color, amount);
    }
}
