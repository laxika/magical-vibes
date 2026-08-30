package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Reveals one opponent's hand for Valki's controller to choose a creature card from it. */
public record ChooseCreatureCardFromTargetHandForValkiEffect(UUID targetPlayerId)
        implements CardEffect {
}
