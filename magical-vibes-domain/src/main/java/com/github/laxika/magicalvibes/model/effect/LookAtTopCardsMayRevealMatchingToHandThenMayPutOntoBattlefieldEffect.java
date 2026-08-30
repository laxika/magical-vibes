package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Looks at the top cards, offers one matching card to reveal and put into hand, then offers that
 * card for the battlefield when it satisfies the mana-value limit.
 */
public record LookAtTopCardsMayRevealMatchingToHandThenMayPutOntoBattlefieldEffect(
        int lookCount,
        CardPredicate predicate,
        int battlefieldManaValueAtMost
) implements CardEffect {
}
