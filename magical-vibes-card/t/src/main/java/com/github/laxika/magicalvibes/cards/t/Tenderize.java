package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TMT", collectorNumber = "133")
public class Tenderize extends Card {

    public Tenderize() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new TargetDealsPowerDamageToTargetEffect());
        target(TargetFilters.creatureAnOpponentControls());
    }
}
