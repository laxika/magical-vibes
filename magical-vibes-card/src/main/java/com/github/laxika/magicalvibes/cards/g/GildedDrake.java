package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "USG", collectorNumber = "76")
public class GildedDrake extends Card {

    public GildedDrake() {
        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        ExchangeControlOfTargetPermanentsEffect.forTriggeringPermanentAndSacrificeIfNoExchange(
                                new PermanentIsCreaturePredicate()));
    }
}
