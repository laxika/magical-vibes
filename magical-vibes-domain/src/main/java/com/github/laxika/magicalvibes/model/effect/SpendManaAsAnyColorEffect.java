package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * "[Players / You] may spend [white] mana as though it were mana of any color" (Mycosynth Lattice, Chromatic Orrery):
 * printed as a static ability, or floated on a player by {@link GrantStaticEffectToPlayerEffect} ("until end of turn,
 * you may spend white mana as though it were mana of any color", False Dawn). Only how a cost is paid changes, and a
 * {C} symbol is still paid only with colorless mana (CR 609.4b, 107.4c). Read by
 * {@code GameQueryService.canSpendManaAsAnyColor} and {@code canSpendWhiteManaAsAnyColorWithoutRestriction}.
 *
 * @param allPlayers whether a printed permission applies to every player rather than just its controller; a floated one
 *                   applies to the player it is on
 * @param fromColor  the color of mana it lets be spent that way ({@link ManaColor#WHITE}), or {@code null} for all mana
 */
public record SpendManaAsAnyColorEffect(boolean allPlayers, ManaColor fromColor) implements CardEffect {

    public SpendManaAsAnyColorEffect {
        if (fromColor != null && fromColor != ManaColor.WHITE) {
            throw new IllegalArgumentException("Only white mana can be singled out to spend as any color, not "
                    + fromColor);
        }
    }

    /** "Players may spend mana as though it were mana of any color". */
    public SpendManaAsAnyColorEffect() {
        this(true);
    }

    /** {@code false}: "you may spend mana as though it were mana of any color". */
    public SpendManaAsAnyColorEffect(boolean allPlayers) {
        this(allPlayers, null);
    }

    /** "You may spend white mana as though it were mana of any color". */
    public static SpendManaAsAnyColorEffect whiteMana() {
        return new SpendManaAsAnyColorEffect(false, ManaColor.WHITE);
    }
}
