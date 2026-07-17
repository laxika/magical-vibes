package com.github.laxika.magicalvibes.model.effect;

/**
 * Static (self-only) effect: "As long as the top card of your library is an artifact or
 * creature card, this creature has all activated abilities of that card."
 * <p>
 * While the controller's top library card is an artifact or creature, its activated abilities
 * are granted to the source permanent. Used by Skill Borrower. Pair with
 * {@link PlayWithTopCardRevealedEffect} for the "play with the top card revealed" clause.
 */
public record GainActivatedAbilitiesOfTopLibraryCardEffect() implements CardEffect {
}
