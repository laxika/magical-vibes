package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "ELD", collectorNumber = "216")
public class ClockworkServant extends Card {

    public ClockworkServant() {
        for (ManaColor color : ManaColor.COLORS) {
            addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                    new ColorSpentToCast(color, 3), new DrawCardEffect(1)));
        }
    }
}
