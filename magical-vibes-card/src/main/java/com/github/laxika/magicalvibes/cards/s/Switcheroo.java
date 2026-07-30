package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M13", collectorNumber = "71")
public class Switcheroo extends Card {

    public Switcheroo() {
        // Exchange control of two target creatures. Either target may be controlled by any player;
        // if both end up under the same controller the exchange does nothing (CR 701.12b).
        target(TargetFilters.creature());
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new ExchangeControlOfTargetPermanentsEffect(
                        new PermanentIsCreaturePredicate(), false, false));
    }
}
