package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Pending accept/decline choice for the current card exiled by Tainted Pact.
 *
 * @param exiledNames names of all cards exiled by this Tainted Pact resolution so far
 */
public record TaintedPactCardChoiceEffect(List<String> exiledNames) implements CardEffect {

    public TaintedPactCardChoiceEffect {
        exiledNames = List.copyOf(exiledNames);
    }
}
