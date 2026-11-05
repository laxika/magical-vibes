package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, registers a delayed trigger that fires at the beginning of the next end step,
 * returning a card from its owner's graveyard to their hand (if it is still there).
 *
 * <p>The card is the stack entry's triggering card, as the trigger event left it in the graveyard: Tiana, Ship's
 * Caretaker's dying Aura or Equipment ("you may return that card to its owner's hand at the beginning of the next
 * end step"), Liesa's dying creature. With no triggering card it is the source card ("when this dies", The Locust
 * God). A card that left the graveyard and entered it again after the ability triggered is a new object
 * (CR 400.7) and is not returned.
 *
 * @param optional whether the registered end-step trigger is a "you may return" ability, so the
 *                 choice is made as that delayed trigger resolves rather than when this one does
 */
public record RegisterDelayedReturnCardFromGraveyardToHandEffect(boolean optional) implements CardEffect {

    public RegisterDelayedReturnCardFromGraveyardToHandEffect() {
        this(false);
    }
}
