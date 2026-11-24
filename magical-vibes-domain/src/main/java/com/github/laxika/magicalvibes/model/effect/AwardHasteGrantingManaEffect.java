package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;

import java.util.Objects;

/**
 * Produces unrestricted mana that carries a rider for the spell it is spent on (CR 106.6): "if that mana is spent on a
 * creature spell, it gains haste until end of turn" (Generator Servant), "… it gains haste" (Hall of the Bandit Lord),
 * "if that mana is spent on an instant or sorcery spell, that spell can't be countered" (Boseiju, Who Shelters All).
 * The mana lands in the regular pool and can be spent on anything; the rider is a tag on that subset of the pool
 * ({@link ManaPool#addHasteGrantingMana}, {@link ManaPool#addUncounterableGrantingMana}, …) which the spell-casting
 * path reads after payment.
 *
 * @param color           the color of mana produced
 * @param amount          how much mana is produced
 * @param creatureSubtype for a haste rider, if non-null only creature spells with this subtype get haste
 * @param rider           what the mana does for the spell it is spent on
 */
public record AwardHasteGrantingManaEffect(ManaColor color, int amount, CardSubtype creatureSubtype, Rider rider)
        implements ManaProducingEffect {

    /** What a unit of the mana does for the spell it is spent on. */
    public enum Rider {
        /** A creature spell gains haste until end of turn. */
        HASTE_UNTIL_END_OF_TURN,
        /** A creature spell gains haste with no duration: the creature keeps it while it stays on the battlefield. */
        PERMANENT_HASTE,
        /** An instant or sorcery spell can't be countered. */
        INSTANT_OR_SORCERY_CANT_BE_COUNTERED
    }

    public AwardHasteGrantingManaEffect {
        Objects.requireNonNull(rider, "rider");
    }

    public AwardHasteGrantingManaEffect(ManaColor color, int amount, CardSubtype creatureSubtype) {
        this(color, amount, creatureSubtype, Rider.HASTE_UNTIL_END_OF_TURN);
    }

    public AwardHasteGrantingManaEffect(ManaColor color, int amount) {
        this(color, amount, null, Rider.HASTE_UNTIL_END_OF_TURN);
    }

    /** Mana whose haste rider has no duration: the creature keeps haste while it stays on the battlefield. */
    public static AwardHasteGrantingManaEffect withPermanentHaste(ManaColor color, int amount) {
        return new AwardHasteGrantingManaEffect(color, amount, null, Rider.PERMANENT_HASTE);
    }

    /** "If that mana is spent on an instant or sorcery spell, that spell can't be countered." */
    public static AwardHasteGrantingManaEffect uncounterableInstantOrSorcery(ManaColor color, int amount) {
        return new AwardHasteGrantingManaEffect(color, amount, null, Rider.INSTANT_OR_SORCERY_CANT_BE_COUNTERED);
    }

    /** The same rider on {@code amount} mana of {@code color} (after a replacement or a multiplier). */
    public AwardHasteGrantingManaEffect withMana(ManaColor color, int amount) {
        return new AwardHasteGrantingManaEffect(color, amount, creatureSubtype, rider);
    }

    public void applyTo(ManaPool pool) {
        pool.add(color, amount);
        switch (rider) {
            case PERMANENT_HASTE -> pool.addPermanentHasteGrantingMana(color, amount);
            case INSTANT_OR_SORCERY_CANT_BE_COUNTERED -> pool.addUncounterableGrantingMana(color, amount);
            case HASTE_UNTIL_END_OF_TURN -> {
                if (creatureSubtype == null) {
                    pool.addHasteGrantingMana(color, amount);
                } else {
                    pool.addSubtypeHasteGrantingMana(creatureSubtype, color, amount);
                }
            }
        }
    }

    /** The rider as the log describes it. */
    public String riderText() {
        return switch (rider) {
            case INSTANT_OR_SORCERY_CANT_BE_COUNTERED -> "an instant or sorcery it is spent on can't be countered";
            case PERMANENT_HASTE -> "grants haste if spent on a creature spell";
            case HASTE_UNTIL_END_OF_TURN -> "grants haste if spent on "
                    + (creatureSubtype == null ? "a creature" : "a " + creatureSubtype.getDisplayName() + " creature")
                    + " spell";
        };
    }
}
