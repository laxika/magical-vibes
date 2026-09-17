package com.github.laxika.magicalvibes.model.effect;

/**
 * Marks a spell-cast trigger effect that needs the triggering spell's mana value snapshotted
 * onto the triggered ability at trigger time.
 */
public interface TriggeringSpellManaValueEffect extends CardEffect {

    /** Returns the trigger-time snapshot of this effect for the supplied spell mana value. */
    default CardEffect snapshotTriggeringSpellManaValue(int manaValue) {
        return this;
    }
}
