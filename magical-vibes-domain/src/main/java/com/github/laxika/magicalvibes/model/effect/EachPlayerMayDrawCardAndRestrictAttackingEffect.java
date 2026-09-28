package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Each player may draw one card; an opponent who does so cannot attack the effect controller
 * during that opponent's next turn.
 */
public record EachPlayerMayDrawCardAndRestrictAttackingEffect() implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }
}
