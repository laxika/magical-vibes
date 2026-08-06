package com.github.laxika.magicalvibes.model.effect;

/**
 * "An opponent chooses target creature they control. Create a token that's a copy of that creature.
 * That token gains haste until end of turn. Exile the token at the beginning of the next end step."
 *
 * <p>The choosing player is an opponent of the ability's controller, but the token is created under
 * the controller's control. With 0 creatures nothing happens; with exactly 1 it is chosen
 * automatically; with 2+ the opponent picks. Echo Chamber.
 */
public record OpponentChoosesCreatureTheyControlTokenCopyEffect() implements CardEffect {
}
