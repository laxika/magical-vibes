package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.UntapAllControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ISD", collectorNumber = "41")
public class VillageBellRinger extends Card {

    public VillageBellRinger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new UntapAllControlledPermanentsEffect(new PermanentIsCreaturePredicate()));
    }
}
