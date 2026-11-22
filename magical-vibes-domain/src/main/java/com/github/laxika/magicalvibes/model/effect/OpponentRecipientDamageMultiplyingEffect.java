package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that multiplies damage dealt to the players {@link #players()} names: the opponents of the effect's
 * controller unless an implementation names others. Implementations can limit the multiplier to player recipients or
 * include permanents controlled by those players.
 */
public interface OpponentRecipientDamageMultiplyingEffect extends CardEffect {

    int damageMultiplier();

    /** Whose damage is multiplied, judged as the damage would be dealt. */
    default PlayerSubject players() {
        return Subjects.eachOpponent();
    }

    /** Whether this multiplier also applies to permanents controlled by the opponent. */
    default boolean appliesToPermanents() {
        return true;
    }
}
