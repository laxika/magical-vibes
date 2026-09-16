package com.github.laxika.magicalvibes.model.effect;

/**
 * Effect that prevents players from casting noncreature spells for the rest of this turn.
 */
public record PlayersCantCastNoncreatureSpellsThisTurnEffect(boolean opponentsOnly) implements CardEffect {

    /** Every player is restricted. */
    public PlayersCantCastNoncreatureSpellsThisTurnEffect() {
        this(false);
    }

    /** Restrict only the opponents of the effect controller. */
    public static PlayersCantCastNoncreatureSpellsThisTurnEffect forOpponents() {
        return new PlayersCantCastNoncreatureSpellsThisTurnEffect(true);
    }
}
