package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Simultaneously untap all tapped [permanents] and tap all untapped [permanents]": every permanent {@code permanents}
 * names flips its tap state. The set is found once and each permanent's tap state is read before either half is
 * applied, so untapping the tapped ones cannot make them eligible for the tapping half.
 *
 * @param permanents which permanents flip: {@code allCreatures()} (Breaking Wave), {@code controlledBy(activePlayer(),
 *                   artifact, creature or land)} ("that player ... they control" of an upkeep trigger, Sands of Time)
 */
public record SimultaneouslyFlipAllPermanentsTapStatesEffect(PermanentSubject permanents) implements CardEffect {

    public SimultaneouslyFlipAllPermanentsTapStatesEffect {
        Objects.requireNonNull(permanents, "permanents");
        if (permanents.declaresTarget()) {
            throw new IllegalArgumentException("The tap-state flip targets nothing: " + permanents);
        }
    }
}
