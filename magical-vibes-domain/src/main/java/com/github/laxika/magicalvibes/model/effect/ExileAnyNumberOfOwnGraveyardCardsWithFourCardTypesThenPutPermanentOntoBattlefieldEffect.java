package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Resolution-time optional graveyard selection whose chosen cards must contain at least four
 * distinct card types collectively; after exiling them, one permanent card among them is returned
 * to the battlefield with the supplied as-enters replacement.
 */
public record ExileAnyNumberOfOwnGraveyardCardsWithFourCardTypesThenPutPermanentOntoBattlefieldEffect(
        EnterWithCountersEffect battlefieldEntryReplacement) implements CardEffect {

    public ExileAnyNumberOfOwnGraveyardCardsWithFourCardTypesThenPutPermanentOntoBattlefieldEffect() {
        this(new EnterWithCountersEffect(CounterType.FINALITY, new Fixed(1)));
    }
}
