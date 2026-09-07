package com.github.laxika.magicalvibes.model.effect;

/**
 * Offers a qualifying spell from the controller's hand for a free cast and creates a token if
 * every offer is declined.
 *
 * @param tokenEffect token created when no qualifying spell is cast
 */
public record MayCastLesserSpellWithSharedTypeOrCreateTokenEffect(CreateTokenEffect tokenEffect)
        implements CardEffect {
}
