package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HOB", collectorNumber = "135")
public class Quarrel extends Card {

    public Quarrel() {
        target(TargetFilters.creatureYouControl());
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new TargetDealsPowerDamageToTargetEffect());
    }
}
