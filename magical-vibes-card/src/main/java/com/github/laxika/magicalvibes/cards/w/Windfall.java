package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsHandThenDrawsGreatestDiscardedEffect;

@CardRegistration(set = "USG", collectorNumber = "111")
@CardRegistration(set = "BRB", collectorNumber = "99")
public class Windfall extends Card {

    public Windfall() {
        addEffect(EffectSlot.SPELL, new EachPlayerDiscardsHandThenDrawsGreatestDiscardedEffect());
    }
}
