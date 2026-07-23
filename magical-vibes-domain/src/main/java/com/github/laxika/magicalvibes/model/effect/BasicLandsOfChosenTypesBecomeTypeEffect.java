package com.github.laxika.magicalvibes.model.effect;

/**
 * Global static effect: basic lands of the first chosen basic land type (stored on the source
 * permanent's {@code chosenSubtype}) become the second chosen basic land type (stored as
 * {@code secondChosenSubtype}). "Basic lands of the first chosen type are the second chosen type."
 *
 * <p>Per MTG rule 305.7, an affected land loses its other land types and abilities, gaining only
 * the intrinsic mana ability of the new basic land type. Nonbasic lands and basics lacking the
 * first chosen type are unaffected. Does not add or remove the snow supertype.
 *
 * <p>Pair with {@link ChooseBasicLandTypeOnEnterEffect}{@code choicesRequired = 2}) in
 * {@code ON_ENTER_BATTLEFIELD}. Used by Illusionary Terrain.
 */
public record BasicLandsOfChosenTypesBecomeTypeEffect() implements CardEffect {
}
