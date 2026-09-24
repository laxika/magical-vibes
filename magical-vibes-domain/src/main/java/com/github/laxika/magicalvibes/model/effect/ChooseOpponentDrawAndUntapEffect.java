package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Resolution-time stage for Intellectual Offering. The controller chooses an opponent, then
 * either both players draw three cards or the controller and that opponent untap their nonlands.
 */
public record ChooseOpponentDrawAndUntapEffect(boolean untapChoice) implements CardDrawingEffect {

    public ChooseOpponentDrawAndUntapEffect() {
        this(false);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(untapChoice ? 0 : 3);
    }
}
