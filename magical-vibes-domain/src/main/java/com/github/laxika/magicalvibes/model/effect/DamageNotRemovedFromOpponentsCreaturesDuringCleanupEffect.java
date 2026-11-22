package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect for "Damage isn't removed from creatures your opponents control during cleanup steps.", or with
 * {@link Subjects#allCreatures()} "... from creatures ..." (the ability Victory of the Pyrohammer gains, which applies
 * to every creature regardless of the creatures' own abilities).
 *
 * @param creatures whose damage stays, judged as the cleanup step removes damage
 */
public record DamageNotRemovedFromOpponentsCreaturesDuringCleanupEffect(PermanentSubject creatures)
        implements OpponentCreatureDamagePersistenceEffect {

    public DamageNotRemovedFromOpponentsCreaturesDuringCleanupEffect {
        if (!(creatures instanceof PermanentSubject.ControlledBy) || creatures.needsStackEntry()) {
            throw new IllegalArgumentException("Damage stays on a static set of creatures: " + creatures);
        }
    }

    public DamageNotRemovedFromOpponentsCreaturesDuringCleanupEffect() {
        this(Subjects.creaturesYourOpponentsControl());
    }
}
