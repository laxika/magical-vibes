package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Forces the targeted player to exile a card from their graveyard.
 * If the exiled card is a creature card, the ability's controller gains life.
 *
 * @param lifeGainIfCreature the amount of life to gain if the exiled card is a creature (0 for no life gain)
 * @param filter restricts the cards the targeted player may choose
 * @param trackWithSource whether the exiled card is tracked with the source permanent
 */
public record TargetPlayerExilesCardFromGraveyardEffect(int lifeGainIfCreature, CardPredicate filter,
                                                         boolean trackWithSource) implements CardEffect {

    public TargetPlayerExilesCardFromGraveyardEffect(int lifeGainIfCreature) {
        this(lifeGainIfCreature, null, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
