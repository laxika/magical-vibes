package com.github.laxika.magicalvibes.model.effect;

/**
 * "Discard a card unless you return a land you control to its owner's hand." (Tragic Lesson)
 * <p>
 * The controller must discard a card unless they choose to return a land they control to its
 * owner's hand instead. If they control no lands, the discard is mandatory. This is the
 * return-a-land counterpart of {@link DiscardUnlessExileCardFromGraveyardEffect}: an optional
 * "you may" escape from the discard, with the escape action chosen at resolution.
 */
public record DiscardUnlessReturnLandToHandEffect() implements CardEffect {
}
