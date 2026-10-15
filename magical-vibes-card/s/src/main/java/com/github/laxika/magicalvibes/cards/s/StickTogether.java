package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesPartyThenSacrificesRestEffect;

@CardRegistration(set = "HBG", collectorNumber = "105")
public class StickTogether extends Card {

    public StickTogether() {
        addEffect(EffectSlot.SPELL, new EachPlayerChoosesPartyThenSacrificesRestEffect());
    }
}
