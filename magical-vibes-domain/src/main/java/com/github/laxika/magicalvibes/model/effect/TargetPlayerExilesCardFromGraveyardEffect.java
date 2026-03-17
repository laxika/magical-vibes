package com.github.laxika.magicalvibes.model.effect;

/**
 * Forces the targeted player to exile a card from their graveyard.
 * If the exiled card is a creature card, the ability's controller gains life.
 *
 * @param lifeGainIfCreature the amount of life to gain if the exiled card is a creature (0 for no life gain)
 */
public record TargetPlayerExilesCardFromGraveyardEffect(int lifeGainIfCreature) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
