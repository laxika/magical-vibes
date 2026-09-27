package com.github.laxika.magicalvibes.model.effect;

/** Makes the owner of the spell that caused this trigger lose the supplied amount of life. */
public record LoseLifeToOwnerOfTriggeringSpellEffect(int amount)
        implements TriggeringSpellManaValueEffect, TriggeringSpellReferencingEffect {

    public LoseLifeToOwnerOfTriggeringSpellEffect() {
        this(0);
    }

    @Override
    public CardEffect snapshotTriggeringSpellManaValue(int manaValue) {
        return new LoseLifeToOwnerOfTriggeringSpellEffect(manaValue);
    }
}
