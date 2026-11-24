package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that multiplies damage dealt to the players {@link #players()} names and to the permanents
 * {@link #permanents()} names: the opponents of the effect's controller and their permanents unless an implementation
 * names others.
 */
public interface OpponentRecipientDamageMultiplyingEffect extends CardEffect {

    int damageMultiplier();

    /** Whose damage is multiplied, judged as the damage would be dealt. */
    default PlayerSubject players() {
        return Subjects.eachOpponent();
    }

    /**
     * Which permanents' damage is multiplied: {@code controlledBy(players())} (the permanents those players control),
     * {@link Subjects#source()} (the permanent with the effect) or {@code null} (none).
     */
    default PermanentSubject permanents() {
        return Subjects.controlledBy(players());
    }
}
