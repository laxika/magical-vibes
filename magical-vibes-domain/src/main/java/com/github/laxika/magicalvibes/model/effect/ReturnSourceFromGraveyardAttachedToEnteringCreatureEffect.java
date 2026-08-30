package com.github.laxika.magicalvibes.model.effect;

/** Returns the source card from its owner's graveyard and attaches it to the creature that caused the trigger. */
public record ReturnSourceFromGraveyardAttachedToEnteringCreatureEffect() implements CardEffect {

    @Override
    public boolean usesEnteringPermanentReference() {
        return true;
    }
}
