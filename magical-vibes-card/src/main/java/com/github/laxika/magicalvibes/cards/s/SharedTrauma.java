package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaThenMillsEffect;

@CardRegistration(set = "CMD", collectorNumber = "99")
public class SharedTrauma extends Card {

    public SharedTrauma() {
        addEffect(EffectSlot.SPELL, new EachPlayerPaysAnyManaThenMillsEffect());
    }
}
