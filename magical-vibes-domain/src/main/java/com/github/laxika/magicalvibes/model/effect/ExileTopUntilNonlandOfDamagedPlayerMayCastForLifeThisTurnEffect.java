package com.github.laxika.magicalvibes.model.effect;

/**
 * Combat-damaged player exiles cards from the top of their library until a nonland is found;
 * the source controller may cast that card during resolution by paying life equal to its mana
 * value rather than its mana cost.
 */
public record ExileTopUntilNonlandOfDamagedPlayerMayCastForLifeThisTurnEffect()
        implements CardEffect {
}
