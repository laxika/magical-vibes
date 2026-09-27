package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Punisher / Rhystic draw: whenever an opponent casts a matching spell, that player may pay
 * {@code payAmount} generic; if they don't, the source's controller may draw {@code drawCount}
 * cards (Mystic Remora, Rhystic Study). The payment can instead be a dynamic amount evaluated
 * when the ability resolves (Esper Sentinel). Ruling order: opponent chooses pay first; only then
 * does the controller choose whether to draw.
 *
 * @param drawCount   cards the source controller may draw if the opponent doesn't pay
 * @param payAmount        generic mana the casting opponent can pay to prevent the draw offer
 * @param spellFilter      optional filter for which spells trigger this (null = any spell)
 * @param dynamicPayAmount optional dynamic payment amount (null = use {@code payAmount})
 */
public record DrawCardUnlessPaysEffect(int drawCount, int payAmount, CardPredicate spellFilter,
                                       DynamicAmount dynamicPayAmount)
        implements CardDrawingEffect {

    public DrawCardUnlessPaysEffect(int drawCount, int payAmount) {
        this(drawCount, payAmount, null, null);
    }

    public DrawCardUnlessPaysEffect(int drawCount, int payAmount, CardPredicate spellFilter) {
        this(drawCount, payAmount, spellFilter, null);
    }

    public DrawCardUnlessPaysEffect(int drawCount, DynamicAmount dynamicPayAmount) {
        this(drawCount, 0, null, dynamicPayAmount);
    }

    public DrawCardUnlessPaysEffect(int drawCount, DynamicAmount dynamicPayAmount, CardPredicate spellFilter) {
        this(drawCount, 0, spellFilter, dynamicPayAmount);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(drawCount);
    }
}
