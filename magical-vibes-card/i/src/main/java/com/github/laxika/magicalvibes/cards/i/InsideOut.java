package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;

@CardRegistration(set = "EVE", collectorNumber = "103")
public class InsideOut extends Card {

    public InsideOut() {
        addEffect(EffectSlot.SPELL, new SwitchPowerToughnessEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
