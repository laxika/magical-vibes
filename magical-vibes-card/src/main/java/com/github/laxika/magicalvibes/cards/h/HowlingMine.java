package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "325")
@CardRegistration(set = "M10", collectorNumber = "212")
@CardRegistration(set = "9ED", collectorNumber = "298")
@CardRegistration(set = "8ED", collectorNumber = "303")
@CardRegistration(set = "7ED", collectorNumber = "300")
@CardRegistration(set = "6ED", collectorNumber = "290")
@CardRegistration(set = "5ED", collectorNumber = "377")
@CardRegistration(set = "4ED", collectorNumber = "325")
public class HowlingMine extends Card {

    public HowlingMine() {
        addEffect(EffectSlot.EACH_DRAW_TRIGGERED, new DrawCardForTargetPlayerEffect(1, true));
    }
}
