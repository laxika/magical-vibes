package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Punisher / Rhystic draw: whenever an opponent casts a matching spell, that player may pay
 * {@code payAmount} generic; if they don't, the source's controller may draw {@code drawCount}
 * cards (Mystic Remora, Rhystic Study). Ruling order: opponent chooses pay first; only then does
 * the controller choose whether to draw.
 *
 * @param drawCount   cards the source controller may draw if the opponent doesn't pay
 * @param payAmount   generic mana the casting opponent can pay to prevent the draw offer
 * @param spellFilter optional filter for which spells trigger this (null = any spell)
 */
public record DrawCardUnlessPaysEffect(int drawCount, int payAmount, CardPredicate spellFilter)
        implements CardDrawingEffect {

    public DrawCardUnlessPaysEffect(int drawCount, int payAmount) {
        this(drawCount, payAmount, null);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(drawCount);
    }
}
