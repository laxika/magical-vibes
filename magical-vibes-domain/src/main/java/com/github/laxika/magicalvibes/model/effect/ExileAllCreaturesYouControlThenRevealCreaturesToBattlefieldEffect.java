package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles all creatures the caster controls, then reveals cards from the top of their library
 * until they reveal that many creature cards. All revealed creature cards are put onto the
 * battlefield, and the rest of the revealed cards are shuffled into their library.
 * <p>
 * Used by Mass Polymorph.
 */
public record ExileAllCreaturesYouControlThenRevealCreaturesToBattlefieldEffect() implements CardEffect {
}
