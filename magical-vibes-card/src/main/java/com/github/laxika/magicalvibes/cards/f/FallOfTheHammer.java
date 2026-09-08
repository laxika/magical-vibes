package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BNG", collectorNumber = "93")
public class FallOfTheHammer extends Card {

    public FallOfTheHammer() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new TargetDealsPowerDamageToTargetEffect());
        target(TargetFilters.creature());
    }
}
