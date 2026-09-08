package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Exiles the top {@code count} cards of the targeted player's library. */
public record ExileTopCardsOfTargetPlayerLibraryEffect(DynamicAmount count) implements CardEffect {

    public ExileTopCardsOfTargetPlayerLibraryEffect(int count) {
        this(new Fixed(count));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
