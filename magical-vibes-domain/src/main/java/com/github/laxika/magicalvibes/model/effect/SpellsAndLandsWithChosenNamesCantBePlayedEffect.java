package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "Spells with the chosen names can't be cast and lands with the chosen names can't
 * be played." (Null Chamber). Reads both {@code chosenName} and {@code secondChosenName} from the
 * source permanent and restricts every player, including the controller.
 *
 * <p>The casting half is enforced in {@code CastingPermissionService.getForbiddenCardNames}; the
 * land half in {@code CastingPermissionService.isLandPlayForbiddenByChosenName}, since land plays
 * deliberately bypass the spell-casting filters. Pair with
 * {@link YouAndOpponentChooseCardNamesOnEnterEffect}.
 */
public record SpellsAndLandsWithChosenNamesCantBePlayedEffect() implements CardEffect {
}
