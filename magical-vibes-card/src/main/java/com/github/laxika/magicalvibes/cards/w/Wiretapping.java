package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintFromTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayImprintedCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "SNC", collectorNumber = "65")
public class Wiretapping extends Card {

    public Wiretapping() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ImprintFromTopCardsEffect(5, true));
        addEffect(EffectSlot.DRAW_TRIGGERED, SequenceEffect.of(
                new DrawCardEffect(1),
                new ConditionalEffect(new CardsInHandAtLeast(9),
                        new PlayImprintedCardWithoutPayingManaCostEffect())
        ));
    }
}
