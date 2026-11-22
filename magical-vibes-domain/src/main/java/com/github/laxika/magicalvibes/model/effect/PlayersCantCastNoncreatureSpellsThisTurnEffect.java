package com.github.laxika.magicalvibes.model.effect;

/**
 * The players {@code players} names as the effect resolves can't cast noncreature spells for the rest of this turn:
 * every player by default (Calamity's Wake), or each opponent of the controller ({@link #forOpponents()}, Permission
 * Denied, Ranger-Captain of Eos).
 *
 * @param players who can't cast; never a target
 */
public record PlayersCantCastNoncreatureSpellsThisTurnEffect(PlayerSubject players) implements CardEffect {

    public PlayersCantCastNoncreatureSpellsThisTurnEffect {
        if (players.declaresTarget()) {
            throw new IllegalArgumentException("The players who can't cast are not targeted: " + players);
        }
    }

    /** Every player is restricted. */
    public PlayersCantCastNoncreatureSpellsThisTurnEffect() {
        this(Subjects.eachPlayer());
    }

    /** Restrict only the opponents of the effect controller. */
    public static PlayersCantCastNoncreatureSpellsThisTurnEffect forOpponents() {
        return new PlayersCantCastNoncreatureSpellsThisTurnEffect(Subjects.eachOpponent());
    }
}
