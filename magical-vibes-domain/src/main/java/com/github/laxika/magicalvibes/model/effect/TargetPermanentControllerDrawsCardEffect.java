package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The controller of the permanent targeted by this stack entry draws cards. Reads the permanent from
 * {@code entry.getTargetId()}, so it piggybacks on a target already chosen by a sibling effect (or the
 * ability's own {@link com.github.laxika.magicalvibes.model.filter.TargetFilter}) — it never contributes
 * a chosen target of its own. Permanent analogue of {@link TargetSpellControllerDrawsCardEffect}. Used by
 * Gwafa Hazid, Profiteer ("Put a bribery counter on target creature you don't control. Its controller
 * draws a card.").
 */
public record TargetPermanentControllerDrawsCardEffect(DynamicAmount amount)
        implements CardEffect, CardDrawingEffect {

    public TargetPermanentControllerDrawsCardEffect() {
        this(1);
    }

    public TargetPermanentControllerDrawsCardEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return amount;
    }
}
