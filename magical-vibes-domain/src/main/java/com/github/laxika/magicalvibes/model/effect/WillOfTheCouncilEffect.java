package com.github.laxika.magicalvibes.model.effect;

/**
 * Starting with the effect controller, each player votes for a nonland permanent the effect
 * controller does not control, or for an eligible card in the controller's graveyard. Permanents
 * tied for the most votes are exiled, while graveyard cards tied for the most votes return to hand.
 */
public record WillOfTheCouncilEffect(boolean graveyardCards) implements CardEffect {

    public WillOfTheCouncilEffect() {
        this(false);
    }
}
