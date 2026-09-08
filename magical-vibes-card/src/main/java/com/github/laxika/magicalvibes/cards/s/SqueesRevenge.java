package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseXValueCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinsUntilLoseEffect;

@CardRegistration(set = "APC", collectorNumber = "123")
public class SqueesRevenge extends Card {

    public SqueesRevenge() {
        addEffect(EffectSlot.SPELL, new ChooseXValueCost(0, Integer.MAX_VALUE));
        addEffect(EffectSlot.SPELL, new FlipCoinsUntilLoseEffect(
                new XValue(), new DrawCardEffect(new Scaled(new XValue(), 2))));
    }
}
