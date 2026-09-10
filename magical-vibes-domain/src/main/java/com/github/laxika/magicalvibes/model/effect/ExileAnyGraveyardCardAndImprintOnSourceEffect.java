package com.github.laxika.magicalvibes.model.effect;

/**
 * During resolution, the controller chooses a card from any graveyard to exile and remember on
 * the source permanent. The exile entry is tracked with the source so later effects can refer to
 * all cards exiled with that permanent.
 */
public record ExileAnyGraveyardCardAndImprintOnSourceEffect() implements CardEffect {
}
