package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

/**
 * The controller chooses a card name, then the target player reveals their hand and discards one
 * card with that name. If they cannot, the controller draws a card.
 */
public record ChooseNameRevealHandDiscardOneOrDrawEffect(List<CardType> excludedTypes)
        implements CardEffect {

    public ChooseNameRevealHandDiscardOneOrDrawEffect {
        excludedTypes = List.copyOf(excludedTypes);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
