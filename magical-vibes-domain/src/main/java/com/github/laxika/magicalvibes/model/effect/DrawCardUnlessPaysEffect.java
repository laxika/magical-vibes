package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Punisher / Rhystic draw: whenever an opponent casts a matching spell (the cast trigger's filter), that player may
 * pay {@code payAmount} generic; if they don't, the source's controller may draw {@code drawCount} cards (Mystic
 * Remora, Rhystic Study). The payment can instead be a dynamic amount evaluated when the ability resolves (Esper
 * Sentinel). Ruling order: opponent chooses pay first; only then does the controller choose whether to draw.
 *
 * @param drawCount        cards the source controller may draw if the opponent doesn't pay
 * @param payAmount        generic mana the casting opponent can pay to prevent the draw offer
 * @param dynamicPayAmount optional dynamic payment amount (null = use {@code payAmount})
 * @param mandatoryDraw    when {@code true} the source controller draws without a may choice once the
 *                         opponent doesn't pay ("draw a card unless that player pays", Esper Sentinel)
 */
public record DrawCardUnlessPaysEffect(int drawCount, int payAmount, DynamicAmount dynamicPayAmount,
                                       boolean mandatoryDraw)
        implements CardDrawingEffect {

    public DrawCardUnlessPaysEffect(int drawCount, int payAmount) {
        this(drawCount, payAmount, null, false);
    }

    public DrawCardUnlessPaysEffect(int drawCount, DynamicAmount dynamicPayAmount) {
        this(drawCount, 0, dynamicPayAmount, false);
    }

    /** "Draw a card unless that player pays {X}" with a mandatory (not may) draw. */
    public static DrawCardUnlessPaysEffect mandatory(int drawCount, DynamicAmount dynamicPayAmount) {
        return new DrawCardUnlessPaysEffect(drawCount, 0, dynamicPayAmount, true);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(drawCount);
    }
}
