package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Looks at the top card of the controller's library. The controller may reveal it; if it matches
 * the predicate, the source permanent transforms. The card stays on top of the library.
 */
public record LookAtTopCardMayRevealMatchingTransformEffect(CardPredicate predicate) implements CardEffect {
}
