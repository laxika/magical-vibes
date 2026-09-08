package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Lets the controller put a matching card from their hand onto the battlefield and, if the card
 * is an artifact, puts the specified counters on the permanent that entered.
 */
public record PutCardFromHandToBattlefieldWithArtifactCountersEffect(
        CardPredicate predicate, String label, int counterCount) implements CardEffect {
}
