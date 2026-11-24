package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: spells with the name (or names) the source permanent chose ({@code permanent.chosenName} and
 * {@code secondChosenName}) can't be cast. When {@code opponentsOnly} is {@code false} the restriction is
 * symmetric — no player can cast such spells (Nevermore); when {@code true} only the controller's opponents are
 * restricted (Gideon's Intervention). With {@code landsToo}, lands with those names can't be played either
 * (Null Chamber: "spells with the chosen names can't be cast and lands with the chosen names can't be played").
 *
 * <p>The casting half is enforced in {@code CastingPermissionService.getForbiddenCardNames}; the land half in
 * {@code CastingPermissionService.isLandPlayForbiddenByChosenName}, since land plays bypass the spell-casting
 * filters.
 */
public record SpellsWithChosenNameCantBeCastEffect(boolean opponentsOnly, boolean landsToo) implements CardEffect {

    public SpellsWithChosenNameCantBeCastEffect() {
        this(false);
    }

    public SpellsWithChosenNameCantBeCastEffect(boolean opponentsOnly) {
        this(opponentsOnly, false);
    }

    /** "Spells with the chosen names can't be cast and lands with the chosen names can't be played." */
    public static SpellsWithChosenNameCantBeCastEffect spellsAndLands() {
        return new SpellsWithChosenNameCantBeCastEffect(false, true);
    }
}
