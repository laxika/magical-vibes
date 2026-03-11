package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerNameCardRevealTopEffect;

@CardRegistration(set = "M11", collectorNumber = "51")
public class ConundrumSphinx extends Card {

    public ConundrumSphinx() {
        addEffect(EffectSlot.ON_ATTACK, new EachPlayerNameCardRevealTopEffect());
    }
}
