package com.github.laxika.magicalvibes.model.effect;

/**
 * Aladdin's Lamp — "{X}, {T}: The next time you would draw a card this turn, instead look at the top
 * X cards of your library, put all but one of them on the bottom of your library in a random order,
 * then draw a card." Registers a one-shot, turn-scoped delayed replacement for the controller's next
 * draw. X is the value paid for the activation (read from the stack entry's {@code xValue}); it is
 * stored in {@link com.github.laxika.magicalvibes.model.GameData#pendingNextDrawLookAtTop} and
 * consumed by {@code DrawService.resolveDrawCard}. The replacement expires at cleanup ("this turn").
 */
public record RegisterNextDrawLookAtTopReplacementEffect() implements CardEffect {
}
