package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Capability for an effect that targets a player and then chooses cards from that player's
 * graveyard for exile.
 */
public interface TargetPlayerGraveyardExileEffect extends CardEffect {

    CardPredicate filter();

    int lifeLossPerExiledCard();

    int lifeGainPerExiledCard();
}
