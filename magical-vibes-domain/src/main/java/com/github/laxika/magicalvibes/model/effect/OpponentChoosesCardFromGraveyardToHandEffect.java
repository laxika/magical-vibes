package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The controller chooses an opponent, then that opponent chooses a matching card from the
 * controller's graveyard and returns it to the controller's hand. The optional defending-player
 * mode is used by attack triggers, where the defending player makes the choice directly; it can
 * also give the source +X/+0 until end of turn, where X is the returned card's mana value.
 */
public record OpponentChoosesCardFromGraveyardToHandEffect(
        CardPredicate filter,
        boolean defendingPlayerChooses,
        boolean boostSourceByChosenCardManaValue) implements CardEffect {

    public OpponentChoosesCardFromGraveyardToHandEffect(CardPredicate filter) {
        this(filter, false, false);
    }
}
