package com.github.laxika.magicalvibes.model.effect;

/**
 * Spell effect: the controller may cast only one additional spell this turn.
 */
public record LimitControllerToOneMoreSpellThisTurnEffect(int maxSpells) implements CardEffect {

    /** The effect a card is built with; resolution carries the spell total it fixed on the effect it makes. */
    public LimitControllerToOneMoreSpellThisTurnEffect() {
        this(Integer.MAX_VALUE);
    }
}
