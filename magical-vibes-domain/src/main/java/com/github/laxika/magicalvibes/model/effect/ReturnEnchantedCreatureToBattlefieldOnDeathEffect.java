package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Death trigger for auras: when the enchanted creature dies, return that creature card from
 * its owner's graveyard to the battlefield. Used by Abduction (owner's control) and by
 * Unhallowed Pact / False Demise ({@code underAuraControllersControl} — the Aura's controller
 * gets it).
 *
 * <p>"That card" is the stack entry's triggering cards ({@code StackEntry.getTriggeringCards()}): the cards the
 * enchanted permanent put into the graveyard (every card of a merged permanent), each with the graveyard incarnation
 * the enchanted-death collector recorded for it (CR 400.7), so a card that left the graveyard and came back is a new
 * object and stays where it is.</p>
 *
 * <p>Contrast {@link ReturnEnchantedCreatureToOwnerHandOnDeathEffect} (Demonic Vigor), which
 * returns the dying creature to its owner's hand instead of the battlefield.</p>
 *
 * @param underAuraControllersControl  {@code true} to put the creature onto the battlefield under
 *                                     the Aura controller's control, {@code false} for its owner's.
 *                                     When the two differ the returned permanent is tracked as a
 *                                     stolen creature so the control change persists.
 * @param enterTapped                   {@code true} if the creature enters the battlefield tapped
 * @param enterWithCounter             optional counter put on the returned permanent as it enters
 */
public record ReturnEnchantedCreatureToBattlefieldOnDeathEffect(
        boolean underAuraControllersControl,
        boolean enterTapped,
        CounterType enterWithCounter
) implements CardEffect {

    /** The "under its owner's control" form. */
    public ReturnEnchantedCreatureToBattlefieldOnDeathEffect() {
        this(false, false, null);
    }

    /** @param underAuraControllersControl see the record component */
    public ReturnEnchantedCreatureToBattlefieldOnDeathEffect(boolean underAuraControllersControl) {
        this(underAuraControllersControl, false, null);
    }

    /**
     * @param underAuraControllersControl see the record component
     * @param enterTapped                  see the record component
     */
    public ReturnEnchantedCreatureToBattlefieldOnDeathEffect(boolean underAuraControllersControl,
                                                              boolean enterTapped) {
        this(underAuraControllersControl, enterTapped, null);
    }

    /**
     * @param underAuraControllersControl see the record component
     * @param enterWithCounter             see the record component
     */
    public ReturnEnchantedCreatureToBattlefieldOnDeathEffect(boolean underAuraControllersControl,
                                                              CounterType enterWithCounter) {
        this(underAuraControllersControl, false, enterWithCounter);
    }
}
