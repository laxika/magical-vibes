package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * "Choose a card name, then target opponent mills a card. If a card with the chosen name was milled
 * this way, you draw a card." (Foreshadow)
 *
 * <p>On resolution the controller names a card, then the target player mills one card. If the milled
 * card's name matches the chosen name and it reached the graveyard, the controller draws a card.
 * Any unconditional follow-up (e.g. delayed next-upkeep draw) is a separate effect on the spell.
 */
public record NameCardMillTargetDrawEffect() implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
