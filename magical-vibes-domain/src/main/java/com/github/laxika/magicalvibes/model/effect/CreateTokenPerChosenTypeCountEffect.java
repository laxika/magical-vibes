package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Prompts for a creature type, then creates one supplied token for each creature of that type
 * controlled by the effect controller.
 */
public record CreateTokenPerChosenTypeCountEffect(CreateTokenEffect tokenEffect)
        implements CardEffect {

    public CreateTokenPerChosenTypeCountEffect {
        Objects.requireNonNull(tokenEffect, "tokenEffect");
    }

}
