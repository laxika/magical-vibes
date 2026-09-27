package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Target player mills {@code count} cards; if at least one {@code cardType} card is put into that
 * player's graveyard this way, the controller draws one card.
 */
public record MillTargetPlayerAndDrawIfTypeMilledEffect(DynamicAmount count, CardType cardType)
        implements CardEffect {

    public MillTargetPlayerAndDrawIfTypeMilledEffect(int count, CardType cardType) {
        this(new Fixed(count), cardType);
    }

    @Override
    public boolean referencesEventValue() {
        return count instanceof EventValue;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
