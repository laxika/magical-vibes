package com.github.laxika.magicalvibes.model.effect;

/**
 * Starting with the effect controller, each player votes for a nonland permanent the effect
 * controller does not control, or for an eligible card in the controller's graveyard. When
 * {@code creaturesOnly} is true, battlefield voting is restricted to creatures. Permanents tied
 * for the most votes are exiled, while graveyard cards tied for the most votes return to hand.
 */
public record WillOfTheCouncilEffect(boolean graveyardCards, boolean creaturesOnly) implements CardEffect {

    public WillOfTheCouncilEffect() {
        this(false, false);
    }

    public WillOfTheCouncilEffect(boolean graveyardCards) {
        this(graveyardCards, false);
    }
}
