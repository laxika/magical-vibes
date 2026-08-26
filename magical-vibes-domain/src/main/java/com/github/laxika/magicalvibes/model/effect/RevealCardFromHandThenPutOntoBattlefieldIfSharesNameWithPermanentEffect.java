package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals one card from the controller's hand and puts it onto the battlefield only if it shares
 * a name with a permanent on a battlefield.
 */
public record RevealCardFromHandThenPutOntoBattlefieldIfSharesNameWithPermanentEffect()
        implements CardEffect {
}
