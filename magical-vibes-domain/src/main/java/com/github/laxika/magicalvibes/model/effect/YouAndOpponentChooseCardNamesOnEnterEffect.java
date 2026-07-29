package com.github.laxika.magicalvibes.model.effect;

/**
 * "As this permanent enters, you and an opponent each choose a card name other than a basic land
 * card name." (Null Chamber). The controller names first, then their opponent; the two names are
 * stamped onto the entering permanent as {@code chosenName} and {@code secondChosenName}.
 *
 * <p>Both choices happen before the permanent enters the battlefield (CR 614.1c), so the effect is
 * handled in the enchantment resolution path rather than by a normal effect handler. Pair it with a
 * STATIC effect that reads both names, e.g. {@link SpellsAndLandsWithChosenNamesCantBePlayedEffect}.
 */
public record YouAndOpponentChooseCardNamesOnEnterEffect() implements CardEffect {
}
