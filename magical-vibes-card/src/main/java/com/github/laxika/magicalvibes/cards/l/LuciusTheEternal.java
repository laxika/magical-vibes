package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSourcePermanentHauntingTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "40K", collectorNumber = "130")
public class LuciusTheEternal extends Card {

    public LuciusTheEternal() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_DEATH, new ExileSourcePermanentHauntingTargetEffect());
    }
}
