package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Capability marker for {@code ON_DAMAGED_CREATURE_DIES} effects that must act on the specific
 * creature card that died. The trigger collector binds the dying creature's card id at collection
 * time, because the "you may …" resolution flow ({@link MayEffect} / {@code PendingMayAbility})
 * does not carry the stack entry's triggering-card id through to the wrapped effect.
 */
public interface DyingCreatureCardAwareEffect {

    /** Returns a copy of this effect with the dying creature's card id bound in. */
    CardEffect boundToDyingCard(UUID dyingCardId);

    /**
     * Returns a copy that also records the dying card's graveyard-entry version, so the effect can
     * tell whether that card is still the object that died. Effects that don't act on the card
     * itself keep the default and return themselves.
     */
    default CardEffect boundToDyingCardEntryVersion(long graveyardEntryVersion) {
        return (CardEffect) this;
    }
}
