package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The defending player chooses one matching card from the ability controller's graveyard and
 * puts it onto the battlefield under the ability controller's control with the effect's fixed
 * reanimation riders.
 */
public record DefendingPlayerChoosesCardFromGraveyardToBattlefieldEffect(CardPredicate filter)
        implements CardEffect {
}
