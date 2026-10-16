package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;

/**
 * Produces unrestricted mana that carries a rider: "If that mana is spent on a creature spell, it
 * gains haste until end of turn" (Generator Servant). The mana lands in the regular pool and can be
 * spent on anything; the rider is a tag on that subset of the pool
 * ({@link ManaPool#addHasteGrantingMana}) which the spell-casting path reads after payment. The
 * optional subtype narrows the rider to creature spells with that subtype.
 *
 * @param color  the color of mana produced
 * @param amount how much mana is produced
 * @param creatureSubtype if non-null, only creature spells with this subtype get haste
 * @param permanentHaste whether the haste has no duration ("it gains haste", Hall of the Bandit Lord)
 *                       instead of lasting until end of turn
 */
public record AwardHasteGrantingManaEffect(ManaColor color, int amount, CardSubtype creatureSubtype,
                                           boolean permanentHaste)
        implements ManaProducingEffect {

    public AwardHasteGrantingManaEffect(ManaColor color, int amount, CardSubtype creatureSubtype) {
        this(color, amount, creatureSubtype, false);
    }

    public AwardHasteGrantingManaEffect(ManaColor color, int amount) {
        this(color, amount, null, false);
    }

    /** Mana whose haste rider has no duration: the creature keeps haste while it stays on the battlefield. */
    public static AwardHasteGrantingManaEffect withPermanentHaste(ManaColor color, int amount) {
        return new AwardHasteGrantingManaEffect(color, amount, null, true);
    }

    public void applyTo(ManaPool pool) {
        pool.add(color, amount);
        if (permanentHaste) {
            pool.addPermanentHasteGrantingMana(color, amount);
        } else if (creatureSubtype == null) {
            pool.addHasteGrantingMana(color, amount);
        } else {
            pool.addSubtypeHasteGrantingMana(creatureSubtype, color, amount);
        }
    }
}
