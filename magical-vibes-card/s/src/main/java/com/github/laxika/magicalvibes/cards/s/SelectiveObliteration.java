package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesColorThenExileOtherPermanentsEffect;

@CardRegistration(set = "M3C", collectorNumber = "35")
public class SelectiveObliteration extends Card {

    public SelectiveObliteration() {
        addEffect(EffectSlot.SPELL, new EachPlayerChoosesColorThenExileOtherPermanentsEffect());
    }
}
