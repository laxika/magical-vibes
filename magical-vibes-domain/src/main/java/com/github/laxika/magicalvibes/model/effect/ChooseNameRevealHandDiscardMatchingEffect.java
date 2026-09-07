package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

/**
 * The controller chooses a card name, then the target player reveals their hand and discards
 * every card with that name. The chosen name is restricted by {@code excludedTypes}.
 */
public record ChooseNameRevealHandDiscardMatchingEffect(List<CardType> excludedTypes)
        implements CardEffect {

    public ChooseNameRevealHandDiscardMatchingEffect {
        excludedTypes = List.copyOf(excludedTypes);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
