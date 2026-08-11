package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAnyNumberEffect;

@CardRegistration(set = "ODY", collectorNumber = "217")
public class RitesOfInitiation extends Card {

    public RitesOfInitiation() {
        addEffect(EffectSlot.SPELL, new DiscardAnyNumberEffect(true));
        addEffect(EffectSlot.SPELL,
                new BoostAllOwnCreaturesEffect(new EventValue(), new Fixed(0)));
    }
}
