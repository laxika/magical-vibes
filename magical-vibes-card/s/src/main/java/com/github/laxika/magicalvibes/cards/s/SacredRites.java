package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAnyNumberEffect;

@CardRegistration(set = "ODY", collectorNumber = "44")
public class SacredRites extends Card {

    public SacredRites() {
        addEffect(EffectSlot.SPELL, new DiscardAnyNumberEffect());
        addEffect(EffectSlot.SPELL,
                new BoostAllOwnCreaturesEffect(new Fixed(0), new EventValue()));
    }
}
