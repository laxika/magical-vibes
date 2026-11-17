package com.github.laxika.magicalvibes.model.effect;

/**
 * Aladdin's Lamp — "{X}, {T}: The next time you would draw a card this turn, instead look at the top
 * X cards of your library, put all but one of them on the bottom of your library in a random order,
 * then draw a card." Registers a one-shot replacement effect (CR 614.1a) for the controller's next
 * draw: as the ability resolves it becomes a floating effect on the controller that ends at cleanup
 * ("this turn") or as it replaces a draw, and whose payload is this record with the {@code x} paid for the
 * activation (read from the stack entry's {@code xValue}). Consumed by {@code DrawService}.
 *
 * @param x the number of cards to look at; {@code 0} on the card's own copy of the record, which the resolution
 *          replaces with the value paid
 */
public record RegisterNextDrawLookAtTopReplacementEffect(int x) implements CardEffect {

    public RegisterNextDrawLookAtTopReplacementEffect() {
        this(0);
    }
}
