package com.github.laxika.magicalvibes.model.effect;

/** Lets the source Saga read ahead, optionally granting read ahead to all Sagas its controller controls. */
public record ReadAheadEffect(boolean appliesToAllControlledSagas) implements CardEffect {

    /** Grants read ahead to all controlled Sagas, as on Barbara Wright. */
    public ReadAheadEffect() {
        this(true);
    }
}
